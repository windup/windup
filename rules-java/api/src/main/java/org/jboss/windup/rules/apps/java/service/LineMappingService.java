package org.jboss.windup.rules.apps.java.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.jboss.forge.roaster._shade.org.eclipse.core.internal.preferences.Base64;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.LineMappingModel;

public class LineMappingService extends GraphService<LineMappingModel> {

    public LineMappingService(GraphContext context) {
        super(context, LineMappingModel.class);
    }

    static final ThreadLocal<Map<Integer, Integer>[]> cache = new ThreadLocal<>();
    static final ThreadLocal<Object[]> ids = new ThreadLocal<>();
    static final int cacheSize = 256; // no need to be large

    public LineMappingModel create(int[] data) {
        LineMappingModel model = create();
        if (data != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            byteBuffer.asIntBuffer().put(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            try {
                defl.write(byteBuffer.array());
                defl.close();
            } catch (IOException e) {
               return model;
            }
            String encodedData = new String(Base64.encode(out.toByteArray()));
            model.setEncodedMapping(encodedData);
        }
        return model;
    }

    public Map<Integer, Integer> getMapping(LineMappingModel model) {

        if (cache.get() == null) {
            cache.set(new Map[cacheSize]);
            ids.set(new Object[cacheSize]);
        }
        Map<Integer, Integer>[] localCache = cache.get();
        Object[] localIds = ids.get();

        int index = (model.getId().hashCode() & Integer.MAX_VALUE) % localCache.length;
        if (model.getId().equals(localIds[index])) {
            return localCache[index];
        }

        Map<Integer, Integer> lineMapping = new HashMap<>();

        String encoded = model.getEncodedMapping();
        if (encoded != null && !encoded.isEmpty()) { 
            byte[] bytes = Base64.decode(encoded.getBytes());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream infl = new InflaterOutputStream(out);
            try {
                infl.write(bytes);
                infl.close();
            } catch (IOException e) {
                return lineMapping;
            }
            IntBuffer data = ByteBuffer.wrap(out.toByteArray()).asIntBuffer();
            while (data.hasRemaining()) {
                lineMapping.put(data.get(), data.get());
            }
        }

        localIds[index] = model.getId();
        localCache[index] = lineMapping;

        return lineMapping;
    }
}
