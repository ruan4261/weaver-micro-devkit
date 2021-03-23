package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocComInfo;
import weaver.docs.docs.DocManager;
import weaver.docs.docs.DocViewer;
import weaver.docs.docs.ShareManageDocOperation;
import weaver.general.TimeUtil;
import weaver.hrm.resource.ResourceComInfo;

/**
 * 生成一篇文章
 *
 * @author 耿守卫
 * @version 2020/4/30 15:00
 */
public class CreateNewsDoc {
    private int maincategory = 0;   //文档主目录
    private int subcategory = 0;    //文档分目录
    private int seccategory;        //文档子目录
    private int docLanguage = 7;    //文档的语言类型 默认为中文

    /**
     * 构造方法，用来获取已经配置的文档的存放目录
     *
     * @param seccategory 文档将会存放到这个目录下
     */
    public CreateNewsDoc(Integer seccategory) {
        this.seccategory = seccategory;
    }

    /**
     * 忽略文档拓展名
     */
    public int createDoc(String title, String body, int creator) throws Exception {
        return createDoc(title, body, creator, null);
    }

    /**
     * 创建一篇文档
     *
     * @param title         文档标题
     * @param body          文档内容  可为空
     * @param creater       创建人id
     * @param docextendname 文档拓展名, 如果当做附件, 此处请填写附件的后缀名
     * @return 返回docid
     */
    public int createDoc(String title, String body, int creater, String docextendname) throws Exception {
        title = body == null ? "null" : title;
        body = body == null ? "" : body;

        ResourceComInfo r = new ResourceComInfo();
        int departmentid = Integer.parseInt(r.getDepartmentID(String.valueOf(creater)));

        SecCategoryComInfo secCategoryComInfo = new SecCategoryComInfo();//子目录信息对象
        DocComInfo docComInfo = new DocComInfo();
        DocManager docManager = new DocManager();
        DocViewer docViewer = new DocViewer();
        String pubishdate = TimeUtil.getCurrentDateString();//发布日期
        String publishtime = TimeUtil.getOnlyCurrentTimeString();//发布时间
        RecordSet recordSet = new RecordSet();
        int newDocId = docManager.getNextDocId(recordSet);
        //先设置分享
        docViewer.setDocShareByDoc(String.valueOf(newDocId));
        docManager.AddShareInfo();
        //设置内容
        docManager.setId(newDocId);//设置新建文档的ID
        docManager.setDocsubject(title);//设置文档标题
        docManager.setMaincategory(maincategory); //设置文档的主目录
        docManager.setSubcategory(subcategory);//用来设置文档的分目录
        docManager.setSeccategory(seccategory);//用来设置文档的子目录
        docManager.setLanguageid(docLanguage); //设置文档的语言
        docManager.setDocstatus("1"); //设置文档的状态,默认为1，生效
        docManager.setDoccreaterid(creater);//设置文档创建者
        docManager.setDocdepartmentid(departmentid);//设置文档创建者部门
        docManager.setDocCreaterType("1");//设置文档创建者类型，1为内部用户
        docManager.setUsertype("1");//设置文档用户类型，此处为人力资源，2为客户
        docManager.setOwnerid(creater); //设置文档拥有者ID
        docManager.setOwnerType("1");//文档拥有者类型，此处为内部用户
        docManager.setDoclastmoduserid(creater);// 文档最后修改者ID
        docManager.setDocLastModUserType("1");//文档最后修改者类型，内部用户
        docManager.setDoccreatedate(pubishdate);//文档创建日期
        docManager.setDoclastmoddate(pubishdate);//文档最后修改日期
        docManager.setDoccreatetime(publishtime); //文档创建时间
        docManager.setDoclastmodtime(publishtime);//文档最后修改时间
        docManager.setDoclangurage(docLanguage);//设置文档语言
        docManager.setKeyword(title);//设置文档关键字
        docManager.setIsapprover("0");//设置该文档不需要流程审批
        docManager.setIsreply("");//设置是否回复文档
        docManager.setDocreplyable("1");//设置文档可以回复
        docManager.setAccessorycount(1);//设置文档附件的数量
        docManager.setParentids(String.valueOf(newDocId));//文档父节点字符串
        docManager.setOrderable(String.valueOf(secCategoryComInfo.getSecOrderable(seccategory)));//文档子目录是不是可以订阅
        docManager.setUserid(creater);
        docManager.setDocCode(""); //设置文档编号
        docManager.setDocEditionId(docManager.getNextEditionId(recordSet)); //设置文档的版本
        docManager.setDocEdition(1);//此处为新建，所以是第一版
        docManager.setDoccontent(body);
        docManager.setDocType(12);
        docManager.setDocextendname(docextendname);
        docManager.AddDocInfo();
        docComInfo.addDocInfoCache(String.valueOf(newDocId));
        //更新 权限
        new ShareManageDocOperation().SynchronousDocShareBySec(
                String.valueOf(seccategory),
                String.valueOf(newDocId),
                String.valueOf(departmentid),
                String.valueOf(creater),
                "1");
        return newDocId;
    }


}
