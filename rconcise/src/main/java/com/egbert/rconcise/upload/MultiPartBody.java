package com.egbert.rconcise.upload;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.egbert.rconcise.internal.Const;
import com.egbert.rconcise.internal.ContentType;
import com.egbert.rconcise.internal.HeaderField;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import static com.egbert.rconcise.internal.Const.CRLF;

/**
 * 用来封装上传文件的请求体body<br><br>
 * Created by Egbert on 4/23/2019.
 */
public class MultiPartBody {

    /**
     * 请求体body中各part的集合, 如果要实现断点上传，bodyParts只能包含<b>一个含有File文件的Part,
     * 而且需要指定上传开始位置{@link Part#beginIndex beginIndex}的值
     */
    private ArrayList<Part> bodyParts;

    public MultiPartBody() {
        bodyParts = new ArrayList<>();
    }

    /**
     *  单个添加part
     */
    public MultiPartBody addPart(Part part) {
        if (part != null) {
            bodyParts.add(part);
        }
        return this;
    }

    /**
     * 批量添加part
     */
    public MultiPartBody addParts(ArrayList<Part> parts) {
        if (parts != null) {
            bodyParts.addAll(parts);
        }
        return this;
    }

    /**
     * @return bodyParts
     */
    public ArrayList<Part> getBodyParts() {
        return bodyParts;
    }

    /**
     * @param content  {@link Part#content(Object content) 见content()}
     * @param name     {@link Part#dispositionName(String)}  见dispositionName()}
     * @return Part
     */
    public static Part createPart(Object content, String name) {
        if (content == null) {
            throw new IllegalArgumentException("The param 'content' can not be null");
        } else if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("The param 'name' can not be null");
        }
        return new Part().content(content)
                .dispositionName(name)
                .build();
    }

    /**
     * <code>Part<code/>用于封装multipart/form-data协议中每个上传块的数据
     */
    public static class Part {
        // 每个part的头信息
        private StringBuilder partHeaders;
        private Object content;
        private String name;
        private String filename;
        // 文件扩展名
        private String extension;
        /**
         * 上传内容的MIME类型 如 text/plain，image/jpeg 等MIME类型
         */
        private String contentType;

        //是否为文件 true是 false否
        private boolean isFile;
        /**
         * 上传文件的开始位置索引，默认从0字节开始上传，要文件断点上传时设置该值，
         * 需要先从你的应用服务器获取上次上传的字节长度即<code>file.length()<code/>,
         * 然后赋值给<code>beginIndex<code/>即可
         */
        private long beginIndex;

        /**
         * 设置Content-Disposition: form-data; name=<code>name</code>; filename=<code>fileName</code>; 中name属性的值
         * @param name 名称
         */
        public Part dispositionName(String name) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("The param 'name' can not be null");
            }
            this.name = name;
            return this;
        }


        /**
         * 设置Content-Disposition: form-data; name=<code>name</code>; filename=<code>fileName</code>; 中filename属性的值
         * @param filename 文件名 如果isFile为true,此参数不能为空,如果不设置，
         *                 默认使用通过File对象获取到的文件名
         * @return Part
         */
        public Part dispositionFilename(String filename) {
            this.filename = filename;
            return this;
        }

        /**
         * 每个part的内容 上传数据的类型T：为String类时原样输出；为其他实体类或Map类时自动转Json串输出；为File时用流输出；
         */
        public Part content(Object content) {
            if (content == null) {
                throw new IllegalArgumentException("The content cannot be null, but it is null now.");
            }
            this.content = content;
            this.isFile = content instanceof File;
            if (isFile) {
                if (TextUtils.isEmpty(filename)) {
                    filename = ((File) content).getName();
                }
                extension = MimeTypeMap.getFileExtensionFromUrl(filename);
                if (TextUtils.isEmpty(extension)) {
                    throw new IllegalArgumentException("The extension is null, the filename is illegal.");
                }
            }
            return this;
        }

        /**
         * @param beginIndex 文件上传的开始位置索引
         * @return Part
         */
        public Part beginIndex(long beginIndex) {
            this.beginIndex = beginIndex;
            return this;
        }

        /**
         * @param contentType 每个上传块的数据的MIME类型, 默认为text/plain;charset=utf-8
         * @return Part
         */
        public Part contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Part build() {
            partHeaders = new StringBuilder();
            partHeaders.append(HeaderField.CONTENT_DISPOSITION.getValue())
                    .append(": ")
                    .append(Const.FORM_DATA)
                    .append(" name=")
                    .append("\"")
                    .append(name)
                    .append("\"");
            if (isFile) {
                partHeaders.append("; ")
                        .append("filename=")
                        .append("\"")
                        .append(filename)
                        .append("\"");
            }
            partHeaders.append(CRLF);
            partHeaders.append(HeaderField.CONTENT_TYPE.getValue())
                    .append(": ");
            if (!TextUtils.isEmpty(contentType)) {
                partHeaders.append(contentType);
                if (!isFile && !contentType.contains(Const.CHARTSET_LABEL)) {
                    if (!contentType.endsWith(";")) {
                        partHeaders.append(";");
                    }
                    partHeaders.append(Const.CHARTSET);
                }
            } else {
                if (!isFile) {
                    partHeaders.append(ContentType.PLAIN.getValue());
                } else {
                    //使用系统API，获取MimeTypeMap的单例实例，然后调用其内部方法获取文件后缀名（扩展名）所对应的MIME类型
                    String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    if (!TextUtils.isEmpty(type)) {
                        partHeaders.append(type);
                    } else {
                        partHeaders.delete(partHeaders.indexOf(HeaderField.CONTENT_TYPE.getValue()) - 2,
                                partHeaders.length());
                    }
                }
            }
            partHeaders.append(CRLF);
            partHeaders.append(CRLF);

            //转换content 不是String和File类型，都转成json字符串输出
            if (!(content instanceof File) && !(content instanceof String)) {
                content = new Gson().toJson(content);
            }
            return this;
        }

        public String getPartHeaders() {
            return partHeaders.toString();
        }

        public Object getContent() {
            return content;
        }

        public long getBeginIndex() {
            return beginIndex;
        }

        /**
         * @return 上传的数据是否为文件，true是，false否
         */
        public boolean isFile() {
            return isFile;
        }
    }
}
