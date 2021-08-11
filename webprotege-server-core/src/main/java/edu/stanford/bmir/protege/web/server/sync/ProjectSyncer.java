package edu.stanford.bmir.protege.web.server.sync;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
//import de.uni_stuttgart.vis.vowl.owl2vowl.Owl2Vowl;
import edu.stanford.bmir.protege.web.server.download.DownloadFormat;
import edu.stanford.bmir.protege.web.server.project.PrefixDeclarationsStore;
import edu.stanford.bmir.protege.web.server.revision.RevisionManager;
import edu.stanford.bmir.protege.web.server.util.MemoryMonitor;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import okhttp3.*;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Date: 07/17/2020
 */
public class ProjectSyncer {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSyncer.class);

    @Nonnull
    private final RevisionNumber revision;

    @Nonnull
    private final DownloadFormat format;

    @Nonnull
    private final String fileName;

    @Nonnull
    private final PrefixDeclarationsStore prefixDeclarationsStore;

    @Nonnull
    private final RevisionManager revisionManager;

    @Nonnull
    private final ProjectId projectId;

    /**
     * Creates a project syncer that syncs the specified revision of the specified project.
     *
     * @param revisionManager         The revision manager of project to be downloaded.  Not <code>null</code>.
     * @param revision                The revision of the project to be downloaded.
     * @param format                  The format which the project should be downloaded in.
     * @param prefixDeclarationsStore The prefix declarations store that is used to retrieve customised prefixes
     */
    @AutoFactory
    @Inject
    public ProjectSyncer(@Nonnull ProjectId projectId,
                         @Nonnull String fileName,
                         @Nonnull RevisionNumber revision,
                         @Nonnull DownloadFormat format,
                         @Nonnull RevisionManager revisionManager,
                         @Provided @Nonnull PrefixDeclarationsStore prefixDeclarationsStore) {
        this.projectId = checkNotNull(projectId);
        this.revision = checkNotNull(revision);
        this.revisionManager = checkNotNull(revisionManager);
        this.format = checkNotNull(format);
        this.fileName = checkNotNull(fileName);
        this.prefixDeclarationsStore = checkNotNull(prefixDeclarationsStore);
    }

    public void writeProject() throws IOException {
        try {
            exportProjectRevision(fileName, revision, format);

        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }

    }

    private void exportProjectRevision(@Nonnull String projectDisplayName,
                                       @Nonnull RevisionNumber revisionNumber,
                                       @Nonnull DownloadFormat format) throws IOException, OWLOntologyStorageException {
        OWLOntologyManager manager = revisionManager.getOntologyManagerForRevision(revisionNumber);
        saveOntologiesToLocalFile(projectDisplayName, manager, format, revisionNumber);
    }

    private void saveOntologiesToLocalFile(@Nonnull String projectDisplayName,
                                           @Nonnull OWLOntologyManager manager,
                                           @Nonnull DownloadFormat format,
                                           @Nonnull RevisionNumber revisionNumber) throws IOException, OWLOntologyStorageException {
        try {
            String owlFilename = "diabetes";


            File owlFile = new File("../" + owlFilename + "." + format.getExtension()); // 相对路径，如果没有则要建立一个新的output.txt文件
            FileOutputStream owlFileOS = new FileOutputStream(owlFile);

            var documentFormat = format.getDocumentFormat();
            if (documentFormat.isPrefixOWLOntologyFormat()) {
                var prefixDocumentFormat = documentFormat.asPrefixOWLOntologyFormat();
                Map<String, String> prefixes = prefixDeclarationsStore.find(projectId).getPrefixes();
                prefixes.forEach(prefixDocumentFormat::setPrefix);
            }

            for (var ontology : manager.getOntologies()) {
                ontology.getOWLOntologyManager().saveOntology(ontology, documentFormat, owlFileOS);
                logMemoryUsage();
            }
            owlFileOS.close();

            try {
                String url = getConverterURL();
                String name = "ontology";
                String fileName = owlFile.getAbsolutePath();
                String jsonContent = postJSONFile(url, name, fileName);

                File jsonFile = new File("../" + owlFilename + ".json"); // 相对路径，如果没有则要建立一个新的output.txt文件
                FileOutputStream jsonFileOS = new FileOutputStream(jsonFile);

                jsonFileOS.write(jsonContent.getBytes(StandardCharsets.UTF_8));
                jsonFileOS.flush();
                jsonFileOS.close();

                url = getWebOWLURL();
                name = owlFilename;
                String filename = owlFilename + ".json";
                String filepath = jsonFile.getAbsolutePath();
                String result = postOWLFile(url, name, filename, filepath);
                logger.info("The result of OWL File uploading to WebOWL is:" + result);
            } catch (Exception e) {
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getConverterURL(){
        String api = "syconvert";
        String prefixUrl = OWLConverterHelper.GenerateUrlPrefix();
        String path = OWLConverterHelper.GetPath();

        String url = prefixUrl + "/" + path + "/" + api;
        return url;
    }

    private String getWebOWLURL(){
        String prefixUrl = WebOWLHelper.GenerateUrlPrefix();
        String path = WebOWLHelper.GetPath();

        String url = prefixUrl + "/" + path;
        return url;
    }

    private String postOWLFile(String url, String name, String filename,  String filepath) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(name, filename, RequestBody.create(MediaType.parse("application/octet-stream"), new File(filepath)))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            return response.body().string();
        } else {
            throw new IOException("Convert Failed");
        }
    }

    private String postJSONFile(String url, String name, String fileName) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(name, fileName, RequestBody.create(MediaType.parse("application/octet-stream"), new File(fileName)))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            return response.body().string();
        } else {
            throw new IOException("Upload Failed");
        }
    }

    private void logMemoryUsage() {
        MemoryMonitor memoryMonitor = new MemoryMonitor(logger);
        memoryMonitor.monitorMemoryUsage();
    }

    private String getOntologyShortForm(OWLOntology ontology) {
        return new OntologyIRIShortFormProvider().getShortForm(ontology);
    }
}
