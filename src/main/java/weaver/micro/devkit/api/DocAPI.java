package weaver.micro.devkit.api;

import static weaver.micro.devkit.core.CacheBase.EMPTY;

import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.general.Util;
import weaver.micro.devkit.io.IOAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 公文、文档操作接口
 *
 * @author ruan4261
 */
public interface DocAPI {

    /**
     * 通过流程requestId获取该流程最新的文档id
     *
     * @param requestId 请求id
     * @return 流程最新文档id
     */
    static String queryDocIdByRequestId(String requestId) {
        if (Util.getIntValue(requestId) == -1) return EMPTY;
        String sql = "select max(id) as id from docdetail where fromworkflow =" + requestId;
        return CommonAPI.querySingleField(sql, "id");
    }

    /**
     * 根据文档id获取最新文件id
     *
     * @param docId 文档id
     * @return 最新文件id
     */
    static String queryImageFileIdLatest(String docId) {
        if (Util.getIntValue(docId) == -1) return EMPTY;
        String sql = "select max(imagefileid) as fid from docimagefile where docid=" + docId;
        return CommonAPI.querySingleField(sql, "fid");
    }

    /**
     * 通过文档id获得最新文件信息，此方法最低兼容E8。
     *
     * @param docId 文档id
     * @return imagefileid 文件id
     *         imagefilename 原文件名
     *         filesize 文件字节
     *         iszip 是否为压缩格式
     *         filerealpath 服务器保存的真实路径
     */
    static Map<String, String> queryImageFileInfo(String docId) {
        if (Util.getIntValue(docId) == -1) return new HashMap<>();

        String fid = queryImageFileIdLatest(docId);
        if ("".equals(fid)) return new HashMap<>();

        RecordSet rs = new RecordSet();
        String sql;
        sql = "select imagefilename,filerealpath,iszip,filesize from imagefile where imagefileid=" + fid;
        rs.execute(sql);
        rs.next();

        Map<String, String> result = new HashMap<>();
        result.put("filerealpath", Util.null2String(rs.getString("filerealpath")));// 真实路径
        result.put("iszip", Util.null2String(rs.getString("iszip")));// 是否zip格式
        result.put("imagefileid", fid);
        result.put("imagefilename", Util.null2String(rs.getString("imagefilename")));// 原文件名
        result.put("filesize", Util.null2String(rs.getString("filesize")));// 字节大小
        return result;
    }

    /**
     * 通过文档id获得输入流并将其内容保存在选定文件夹中。
     *
     * @param docId    文档id
     * @param path     保存文件到该文件夹下
     * @param filename 文件名称，此入参为空时使用数据库保存的文档名
     * @param charset  如此参数不为空，将使用对应字符流，如参数为空，则使用字节流
     * @return 保存的完整路径
     */
    static String saveDocLocally(String docId, String path, String filename, String charset) {
        if (Util.getIntValue(docId) == -1) return EMPTY;

        Map<String, String> imageFileInfo = queryImageFileInfo(docId);
        String fid = imageFileInfo.get("imagefileid");
        if (fid == null || "".equals(fid)) return EMPTY;

        InputStream inputStream;
        try {
            inputStream = ImageFileManager.getInputStreamById(Integer.parseInt(fid));
        } catch (NumberFormatException e) {
            return EMPTY;
        }

        // 如果filename参数为空，则使用真实文件名作为保存的文件名
        String name = Util.null2String(filename).equals("") ? Util.null2String(imageFileInfo.get("imagefilename")) : filename;
        String savePath;
        if (path.endsWith(File.separator)) savePath = path + name;
        else savePath = path + File.separator + name;

        try {
            if (charset == null || charset.equals(""))// 无字符集使用字节流
                IOAPI.inputStreamSaveLocally(savePath, inputStream, false);
            else// 有字符集使用字符流
                IOAPI.inputStreamSaveLocallyCharset(savePath, inputStream, false, charset);
        } catch (IOException e) {
            return EMPTY;
        }
        return savePath;
    }
}
