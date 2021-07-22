package weaver.micro.devkit.print.model;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.print.MinimumTypeModel;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class DelegateMinimumTypeModel implements MinimumTypeModel {

    private final PriorityQueue<SortableMinimumTypeModel> queue
            = new PriorityQueue<SortableMinimumTypeModel>(
            11,
            new Comparator<SortableMinimumTypeModel>() {

                @Override
                public int compare(SortableMinimumTypeModel o1, SortableMinimumTypeModel o2) {
                    return o1.order - o2.order;
                }

            });

    @Override
    public boolean isMinimumType(Class<?> type) {
        Iterator<? extends MinimumTypeModel> it = this.iterator();
        while (it.hasNext()) {
            MinimumTypeModel model = it.next();
            if (model.isMinimumType(type))
                return true;
        }
        return false;
    }

    @Override
    public MinimumType get(Class<?> type) {
        MinimumType ret;
        Iterator<? extends MinimumTypeModel> it = this.iterator();
        while (it.hasNext()) {
            MinimumTypeModel model = it.next();
            if ((ret = model.get(type)) != null)
                return ret;
        }
        return null;
    }

    public void add(int order, MinimumTypeModel model) {
        SortableMinimumTypeModel sortable = new SortableMinimumTypeModel(order, model);
        this.queue.add(sortable);
    }

    public Iterator<? extends MinimumTypeModel> iterator() {
        return this.queue.iterator();
    }

    public int size() {
        return this.queue.size();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    private static class SortableMinimumTypeModel implements MinimumTypeModel {

        private final int order;

        private final MinimumTypeModel actual;

        public SortableMinimumTypeModel(int order, MinimumTypeModel actual) {
            this.order = order;
            this.actual = Assert.notNull(actual);
        }

        public int getOrder() {
            return this.order;
        }

        @Override
        public boolean isMinimumType(Class<?> type) {
            return this.actual.isMinimumType(type);
        }

        @Override
        public MinimumType get(Class<?> type) {
            return this.actual.get(type);
        }

    }

}
