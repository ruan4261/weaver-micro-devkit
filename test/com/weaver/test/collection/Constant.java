package com.weaver.test.collection;

import weaver.micro.devkit.util.Collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public final class Constant {

    /**
     * 1: 使用axis方式调用webservice
     * 2: 使用http模拟报文方式调用webservice
     */
    public final static int WebServiceCallSign = 1;

    /**
     * 航信webservice地址, 末尾?wsdl不需要
     */
    public final static String HangXingWebServiceAddress = "http://10.10.8.151:8080/hansen_service_sys/demo/api";

    /* 网报4条流程相关信息 */

    public final static int WorkflowId1903 = 12823;
    public final static int WorkflowId1904 = 12824;
    public final static int WorkflowId1922 = Integer.MAX_VALUE;
    public final static int WorkflowId1923 = Integer.MAX_VALUE;

    public final static int BillId1903;
    public final static int BillId1904;
    public final static int BillId1922;
    public final static int BillId1923;

    private final static int[] WorkflowIdArray;

    static {
//        BillId1903 = WorkflowAPI.getBillIdByWorkflowId(WorkflowId1903);
//        BillId1904 = WorkflowAPI.getBillIdByWorkflowId(WorkflowId1904);
//        BillId1922 = WorkflowAPI.getBillIdByWorkflowId(WorkflowId1922);
//        BillId1923 = WorkflowAPI.getBillIdByWorkflowId(WorkflowId1923);
        BillId1903 = 0;
        BillId1904 = 0;
        BillId1922 = 0;
        BillId1923 = 0;
        WorkflowIdArray = new int[]{
                WorkflowId1903,
                WorkflowId1904,
                WorkflowId1922,
                WorkflowId1923
        };
    }

    static Collections.MapFiller<Integer, ResolveMode> f;

    public static void main(String[] args) {
        int[] b = WorkflowIdArray.clone();
        System.out.println(WorkflowIdArray);
        System.out.println(b);
        System.out.println(Arrays.toString(b));
        System.out.println(f);
        f.put(1, ResolveMode.CashReimb);
    }

    public static int[] getWorkflowIdArray() {
        int[] a = new int[WorkflowIdArray.length];
        System.arraycopy(WorkflowIdArray, 0, a, 0, a.length);
        return a;
    }

    /* 映射信息 */

    /**
     * 每条流程可接收数据的节点映射
     *
     * key: 流程路径编号
     * value: 允许接收数据的节点集合
     */
    static final Map<Integer, List<Integer>> AvailableNodeMapper
            = weaver.micro.devkit.util.Collections.immutableMap(
            new weaver.micro.devkit.util.Collections.MapConstructor<Integer, List<Integer>>() {

                @Override
                public void construct(Collections.MapFiller<Integer, List<Integer>> filler) {
                    filler.put(WorkflowId1903, new ArrayList<Integer>(java.util.Collections.singletonList(27017)));
                    filler.put(WorkflowId1904, new ArrayList<Integer>(java.util.Collections.singletonList(27022)));
                    filler.put(WorkflowId1922, new ArrayList<Integer>(java.util.Collections.singletonList(-1)));
                    filler.put(WorkflowId1923, new ArrayList<Integer>(java.util.Collections.singletonList(-1)));
                }

            });

    public static List<Integer> queryAvailableNode(int workflowId) {
        return AvailableNodeMapper.get(workflowId);
    }

    /**
     * 根据流程切换不同操作模式
     */
    final static Map<Integer, ResolveMode> BillResolveModeMapper
            = Collections.immutableMap(
            new Collections.MapConstructor<Integer, ResolveMode>() {

                @Override
                public void construct(Collections.MapFiller<Integer, ResolveMode> filler) {
                    filler.put(Constant.BillId1903, ResolveMode.TravelReimb);
                    filler.put(Constant.BillId1904, ResolveMode.CashReimb);
                    filler.put(Constant.BillId1922, ResolveMode.TravelReimb);
                    filler.put(Constant.BillId1923, ResolveMode.CashReimb);
                    f = filler;
                }

            });

    public static ResolveMode queryResolveMode(int billId) {
        ResolveMode mode = BillResolveModeMapper.get(billId);
        return mode == null ? ResolveMode.Disable : mode;
    }

}
