package arkhados.effects;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author william
 */
public class LightningTest {

    public LightningTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createGeometry method, of class Lightning.
     */
    @Test
    public void testCreateGeometry() {
        System.out.println("createGeometry");
        float jitter = 0.0F;
        float widthFactor = 0.0F;
        for (int i = 0; i < 100; i++) {
            Mesh result = Lightning.createGeometry(jitter, widthFactor, 0f);
        }
        

        // TODO review the generated test code and remove the default call to fail.
    }

    @Test
    public void testCreateBranchlessSegments() throws
            NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method method;
        method = Lightning.class.getDeclaredMethod(
                "generateSegments", float.class, float.class);
        method.setAccessible(true);

        List<Segment> segments = (List<Segment>) method.invoke(null, 0f, 0f);
        for (int i = 1; i < segments.size(); i++) {
            Segment prev = segments.get(i - 1);
            Segment current = segments.get(i);
            assertSame(prev.second, current.first);
        }

        assertEquals(Vector3f.ZERO, segments.get(0).first);
    }

    @Test
    public void testCreateBranchySegments() throws
            NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method method;
        method = Lightning.class.getDeclaredMethod(
                "generateSegments", float.class, float.class);
        method.setAccessible(true);

        for (int testRun = 0; testRun < 100; testRun++) {
            List<Segment> segments = (List<Segment>) method.invoke(
                    null, 0f, 0.5f);
            assertEquals(Vector3f.ZERO, segments.get(0).first);
        }
    }
}
