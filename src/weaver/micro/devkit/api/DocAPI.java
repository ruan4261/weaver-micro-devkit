package weaver.micro.devkit.api;

import weaver.docs.docs.DocImageManager;
import weaver.docs.docs.VersionIdUpdate;
import weaver.file.ImageFileManager;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.handler.StrictRecordSet;

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
     * @deprecated 请使用#getDocIdByRequestId(int)
     */
    @Deprecated
    public static String queryDocIdByRequestId(int requestId) {
        return CommonAPI.querySingleField(
                "select max(id) as id from docdetail where fromworkflow = ?", requestId);
    }

    /**
     * 获取流程相关的最新文档
     */
    public static int getDocIdByRequestId(int requestId) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select max(id) as id from docdetail where fromworkflow = ?", requestId));
    }

    /**
     * 根据文档id获取最新文件id
     *
     * @param docId 文档id
     * @return 最新文件id
     * @deprecated 返回值不正确
     */
    public static String queryImageFileIdLatest(int docId) {
        return CommonAPI.querySingleField(
                "select max(imagefileid) as fid from docimagefile where docid = ?", docId);
    }

    /**
     * 根据文档id获取最新附件id
     *
     * @param docId 文档id
     * @return 最新附件id
     */
    public static int getImageFileIdByDocId(int docId) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select max(imagefileid) as fid from docimagefile where docid = ?", docId));
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
    public static Map<String, String> queryImageFileInfo(int docId) {
        String fid = queryImageFileIdLatest(docId);
        return CommonAPI.queryOneRow("select imagefileid, imagefilename, filerealpath, iszip, filesize" +
                " from imagefile where imagefileid = ?", fid);
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
        StrictRecordSet rs = new StrictRecordSet();
        DocImageManager dm = new DocImageManager();
        VersionIdUpdate versionIdUpdate = new VersionIdUpdate();
        int versionid = versionIdUpdate.getVersionNewId();

        try {
            rs.executeUpdate("insert into DocImageFile (id, docid, imagefileid, imagefilename, imagefilewidth," +
                            " imagefileheight, imagefielsize, docfiletype, versionid)" +
                            " values(?, ?, ?, ?, '0', '0', '0', '3', ?)",
                    dm.getNextDocImageFileId(), docId, imageFileId, fileName, versionid);
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
