package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.docs.docs.DocImageManager;
import weaver.docs.docs.VersionIdUpdate;
import weaver.file.ImageFileManager;
import weaver.general.Util;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.io.LocalAPI;

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
public final class DocAPI {

    /**
     * 通过流程requestId获取该流程最新的文档id
     *
     * @param requestId 请求id
     * @return 流程最新文档id
     * @deprecated 返回值不正确
     */
    @Deprecated
    public static String queryDocIdByRequestId(final int requestId) {
        String sql = "select max(id) as id from docdetail where fromworkflow =" + requestId;
        return CommonAPI.querySingleField(sql, "id");
    }

    /**
     * 获取流程相关的最新文档
     */
    public static int getDocIdByRequestId(final int requestId) {
        String sql = "select max(id) as id from docdetail where fromworkflow =" + requestId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "id"));
    }

    /**
     * 根据文档id获取最新文件id
     *
     * @param docId 文档id
     * @return 最新文件id
     * @deprecated 返回值不正确
     */
    public static String queryImageFileIdLatest(final int docId) {
        String sql = "select max(imagefileid) as fid from docimagefile where docid=" + docId;
        return CommonAPI.querySingleField(sql, "fid");
    }

    /**
     * 根据文档id获取最新附件id
     *
     * @param docId 文档id
     * @return 最新附件id
     */
    public static int getImageFileIdByDocId(final int docId) {
        String sql = "select max(imagefileid) as fid from docimagefile where docid=" + docId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "fid"));
    }

    /**
     * 通过文档id获得最新文件信息。
     *
     * @param docId 文档id
     * @return imagefileid 文件id
     *         imagefilename 原文件名
     *         filesize 文件字节
     *         iszip 是否为压缩格式
     *         filerealpath 服务器保存的真实路径
     */
    public static Map<String, String> queryImageFileInfo(final int docId) {
        String fid = queryImageFileIdLatest(docId);
        if ("".equals(fid)) return new HashMap<String, String>();

        RecordSet rs = new RecordSet();
        String sql;
        sql = "select imagefilename,filerealpath,iszip,filesize from imagefile where imagefileid=" + fid;
        rs.execute(sql);
        rs.next();

        Map<String, String> result = new HashMap<String, String>();
        result.put("filerealpath", Util.null2String(rs.getString("filerealpath")));// 真实路径
        result.put("iszip", Util.null2String(rs.getString("iszip")));// 是否zip格式
        result.put("imagefileid", fid);
        result.put("imagefilename", Util.null2String(rs.getString("imagefilename")));// 原文件名
        result.put("filesize", Util.null2String(rs.getString("filesize")));// 字节大小
        return result;
    }

    /**
     * 通过文档id获得最新附件并将其保存在选定文件夹中。
     *
     * @param docId      文档id
     * @param saveFolder 保存文件到该文件夹下
     * @param filename   文件名称，此入参为空时使用数据库保存的文档名
     * @param charset    如此参数不为空，将使用对应字符流，如参数为空，则使用字节流
     * @return 保存的完整路径
     */
    public static String saveDocLocally(int docId, String saveFolder, String filename, String charset) {
        Assert.notEmpty(saveFolder, "path");

        Map<String, String> imageFileInfo = queryImageFileInfo(docId);
        String fid = imageFileInfo.get("imagefileid");
        if (fid == null || "".equals(fid))
            return "";

        // 如果filename参数为空，则使用真实文件名作为保存的文件名
        String name = Util.null2String(filename).equals("") ? Util.null2String(imageFileInfo.get("imagefilename")) : filename;
        String savePath;
        if (saveFolder.endsWith(File.separator)) savePath = saveFolder + name;
        else savePath = saveFolder + File.separator + name;

        InputStream inputStream = null;
        try {
            inputStream = ImageFileManager.getInputStreamById(Integer.parseInt(fid));

            if (charset == null || charset.equals(""))// 无字符集使用字节流
                LocalAPI.saveByteStream(inputStream, savePath);
            else// 有字符集使用字符流
                LocalAPI.saveCharStream(inputStream, savePath, charset);
        } catch (NumberFormatException e) {
            return "";
        } catch (IOException e) {
            return "";
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return savePath;
    }

    /**
     * @param detailedCategory 细粒度最小存放目录
     * @param title            文档标题
     * @param content          文档内容
     * @param creator          创建者, 所属人
     * @return 存放的文档id
     */
    public static int autoArchiving(int detailedCategory, String title, String content, int creator) throws Exception {
        // 文档存放目录
        CreateNewsDoc createNewsDoc = new CreateNewsDoc(detailedCategory);
        return createNewsDoc.createDoc(title, content, creator);
    }

    /**
     * 将文件作为文档附件
     *
     * @param docId       文档id
     * @param imageFileId 文件id
     * @param fileName    文件名称
     */
    public static void createDocImageFile(int docId, int imageFileId, String fileName) {
        RecordSet rs = new RecordSet();
        DocImageManager dm = new DocImageManager();
        VersionIdUpdate versionIdUpdate = new VersionIdUpdate();
        int versionid = versionIdUpdate.getVersionNewId();

        try {
            rs.execute("insert into DocImageFile (id,docid,imagefileid,imagefilename,imagefilewidth,imagefileheight,imagefielsize,docfiletype,versionid)" +
                    " values(" + dm.getNextDocImageFileId() + "," + docId + "," + imageFileId + ",'" + fileName + "','0','0','0','3'," + versionid + ")");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个oa文件
     *
     * @param fileName 文件名
     * @param data     文件内容
     * @return 在表ImageFile中的id
     */
    public static int createImageFile(String fileName, byte[] data) {
        ImageFileManager imageFileManager = new ImageFileManager();
        imageFileManager.setImagFileName(fileName);
        imageFileManager.setData(data);
        return imageFileManager.saveImageFile();
    }

    /**
     * 创建附件文档
     *
     * @param title    文档标题
     * @param fileName 文件名
     * @param data     文件数据
     * @param category 子文档目录
     * @param creator  文档创建者
     */
    public static int createFileDoc(String title, String fileName, byte[] data, int category, int creator) {
        int imageFileId = createImageFile(fileName, data);
        try {
            // 获取附件后缀名
            String extName = null;
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot != -1) {
                extName = fileName.substring(lastDot + 1);
            }

            CreateNewsDoc createNewsDoc = new CreateNewsDoc(category);
            int docId = createNewsDoc.createDoc(title, null, creator, extName);
            createDocImageFile(docId, imageFileId, fileName);
            return docId;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
