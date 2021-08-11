package edu.stanford.bmir.protege.web.client.projectmanager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.http.client.*;
import edu.stanford.bmir.protege.web.client.download.DownloadSettingsDialog;
import edu.stanford.bmir.protege.web.client.download.ProjectRevisionDownloader;
import edu.stanford.bmir.protege.web.client.library.msgbox.MessageBox;
import edu.stanford.bmir.protege.web.shared.download.DownloadFormatExtension;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.shared.download.ProjectDownloadConstants.*;

/**
 * 添加同步数据的实现类
 */
public class SyncProjectRequestHandlerImpl implements SyncProjectRequestHandler {

    @Nonnull
    private final MessageBox messageBox;

    @Inject
    public SyncProjectRequestHandlerImpl(@Nonnull MessageBox messageBox) {
        this.messageBox = messageBox;
    }

    @Override
    public void handleProjectSyncRequest(final ProjectId projectId) {
//        MessageBox messageBox = this.messageBoxProvider.get();
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
            }

            @Override
            public void onSuccess() {
                String baseURL = GWT.getHostPageBaseURL();
                String encodedProjectName = URL.encode(projectId.getId());
                RevisionNumber head = checkNotNull(RevisionNumber.getHeadRevisionNumber());
                String syncURL = baseURL + "sync?"
                        + PROJECT + "=" + encodedProjectName +
                        "&" + REVISION + "=" + head.getValue() +
                        "&" + FORMAT + "=owl";
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(syncURL)); //若url中包含无效字符（像空格等），需要进行编码

                builder.setCallback(new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        // 异常处理
                        messageBox.showAlert("同步失败", exception.getMessage());
                    }

                    public void onResponseReceived(Request request, Response response) {
                        // 根据响应状态码进行处理
                        if (Response.SC_OK == response.getStatusCode()) {
                            // 成功响应的处理
                            messageBox.showMessage("同步成功", "数据已成功同步至可视化界面");
                            // response.getText();
                        } else {
                            int code = response.getStatusCode();
                            messageBox.showAlert("同步失败", String.valueOf(code));
                        }
                        // 其它状态的处理，可选
                    }
                });

                // 发送请求
                try {
                    builder.send();
                } catch (RequestException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
