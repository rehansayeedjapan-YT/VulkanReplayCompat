
import java.lang.reflect.Field;
public class dump_vf {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.client.render.VertexFormats");
        for (Field f : clazz.getDeclaredFields()) {
            System.out.println(f.getName() + " : " + f.getType().getName());
        }
    }
}

