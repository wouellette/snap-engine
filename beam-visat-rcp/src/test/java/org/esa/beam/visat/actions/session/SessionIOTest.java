package org.esa.beam.visat.actions.session;

import junit.framework.TestCase;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.toolviews.layermanager.LayerEditor;

import java.awt.Rectangle;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import com.bc.ceres.core.ExtensionManager;
import com.bc.ceres.core.SingleTypeExtensionFactory;
import com.bc.ceres.glayer.LayerType;

public class SessionIOTest extends TestCase {
    private static interface LayerIO {

    }

    public void testGetInstance() {
        assertNotNull(SessionIO.getInstance());
        assertSame(SessionIO.getInstance(), SessionIO.getInstance());
    }

    public void testIO() throws Exception {
        ExtensionManager.getInstance().register(LayerType.class, new GraticuleLayerIOFactory());


        final Session session1 = SessionTest.createTestSession();

        testSession(session1);
        final StringWriter writer = new StringWriter();
        SessionIO.getInstance().writeSession(session1, writer);
        final String xml = writer.toString();
        System.out.println("Session XML:\n" + xml);
        final StringReader reader = new StringReader(xml);
        final Session session2 = SessionIO.getInstance().readSession(reader);
        testSession(session2);
    }

    private void testSession(Session session) {
        assertEquals(Session.CURRENT_MODEL_VERSION, session.getModelVersion());

        assertEquals(2, session.getProductCount());
        testProductRef(session.getProductRef(0), 11, new File("testdata/out/DIMAP/X.dim"));
        testProductRef(session.getProductRef(1), 15, new File("testdata/out/DIMAP/Y.dim"));

        assertEquals(4, session.getViewCount());
        testViewRef(session.getViewRef(0), 0, ProductSceneView.class.getName(), new Rectangle(0, 0, 200, 100), 11, "A");
        testViewRef(session.getViewRef(1), 1, ProductSceneView.class.getName(), new Rectangle(200, 0, 200, 100), 15, "C");
        testViewRef(session.getViewRef(2), 2, ProductSceneView.class.getName(), new Rectangle(0, 100, 200, 100), 11, "B");
        testViewRef(session.getViewRef(3), 3, ProductSceneView.class.getName(), new Rectangle(200, 100, 200, 100), 15, "D");

        assertEquals(2, session.getViewRef(3).getLayerCount());
        assertEquals("[15] D", session.getViewRef(3).getLayerRef(0).name);
        assertEquals("Graticule", session.getViewRef(3).getLayerRef(1).name);
    }

    private void testProductRef(Session.ProductRef productRef, int expectedId, File expectedFile) {
        assertEquals(expectedId, productRef.id);
        assertEquals(expectedFile, productRef.file);
    }

    private void testViewRef(Session.ViewRef viewRef, int expectedId, String expectedType, Rectangle expectedBounds, int expectedProductId, String expectedProductNodeName) {
        assertEquals(expectedId, viewRef.id);
        assertEquals(expectedType, viewRef.type);
        assertEquals(expectedBounds, viewRef.bounds);
        assertEquals(expectedProductId, viewRef.productId);
        assertEquals(expectedProductNodeName, viewRef.productNodeName);
    }

    static class GraticuleLayerIOFactory extends SingleTypeExtensionFactory<LayerType, LayerIO> {
        private GraticuleLayerIOFactory() {
            super(LayerIO.class, GraticuleLayerIO.class);
        }

        @Override
        protected LayerIO getExtensionImpl(LayerType layerType, Class<LayerIO> extensionType) throws Throwable {
            return new GraticuleLayerIO();
        }
    }

    static class GraticuleLayerIO implements LayerIO {
    }

}
