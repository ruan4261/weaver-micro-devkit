package weaver.micro.devkit.xml;

import weaver.micro.devkit.Cast;
import weaver.micro.devkit.util.BeanUtils;

import java.util.Map;

public class XMLUtils {

    public static XMLElement create(Object obj, String name) {
        Map<String, Object> attrs = BeanUtils.object2Map(obj, false, 128);
        XMLElement root = new XMLElement(name);
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || key.isEmpty() || value == null)
                continue;

            if (value instanceof XMLAware) {
                root.addChild(XMLUtils.create(value, key));
            } else {
                XMLElement child = new XMLElement(key);
                child.setContent(Cast.o2String(value));
                root.addChild(child);
            }
        }
        return root;
    }

    public static boolean isBlank(XMLElement element) {
        return element.getAttributes().isEmpty() &&
                element.getChildren().isEmpty() &&
                element.getContent() == null;
    }

}
