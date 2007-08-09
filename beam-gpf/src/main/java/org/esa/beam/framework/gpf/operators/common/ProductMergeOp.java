/*
 * $Id: ProductMergeOp.java,v 1.3 2007/05/14 12:25:40 marcoz Exp $
 *
 * Copyright (C) 2007 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.framework.gpf.operators.common;

import com.bc.ceres.core.ProgressMonitor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDomReader;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.*;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.StringUtils;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: 1.3 $ $Date: 2007/05/14 12:25:40 $
 */
public class ProductMergeOp extends AbstractOperator implements ParameterConverter {

    public static class Configuration {
        private String productType = "no";
        private String baseGeoInfo;
        private List<BandDesc> bands;

        public Configuration() {
            bands = new ArrayList<BandDesc>();
        }
    }

    @Parameter
    private Configuration config;
    private Map<RasterDataNode, Band> bandMapping;

    public ProductMergeOp(OperatorSpi spi) {
        super(spi);
        config = new Configuration();
        bandMapping = new HashMap<RasterDataNode, Band>();
    }

    public void getParameterValues(Operator operator, Xpp3Dom configuration) throws OperatorException {
         // todo - implement
    }

    public void setParameterValues(Operator operator, Xpp3Dom configuration) throws OperatorException {
        XStream xStream = new XStream();
        xStream.setClassLoader(this.getClass().getClassLoader());
        xStream.alias(configuration.getName(), Configuration.class);
        xStream.alias("band", BandDesc.class);
        xStream.addImplicitCollection(Configuration.class, "bands");
        xStream.unmarshal(new XppDomReader(configuration), config);
    }

    @Override
    protected Product initialize(ProgressMonitor pm) throws OperatorException {

        Product outputProduct;
        if (StringUtils.isNotNullAndNotEmpty(config.baseGeoInfo)) {
            Product baseGeoProduct = getContext().getSourceProduct(config.baseGeoInfo);
            final int sceneRasterWidth = baseGeoProduct.getSceneRasterWidth();
            final int sceneRasterHeight = baseGeoProduct.getSceneRasterHeight();
            outputProduct = new Product("mergedName", config.productType,
                                        sceneRasterWidth, sceneRasterHeight);

            copyBaseGeoInfo(baseGeoProduct, outputProduct);
        } else {
            BandDesc bandDesc = config.bands.get(0);
            Product srcProduct = getContext().getSourceProduct(bandDesc.product);
            final int sceneRasterWidth = srcProduct.getSceneRasterWidth();
            final int sceneRasterHeight = srcProduct.getSceneRasterHeight();
            outputProduct = new Product("mergedName", config.productType,
                                        sceneRasterWidth, sceneRasterHeight);
        }

        Set<Product> allSrcProducts = new HashSet<Product>();
        for (BandDesc bandDesc : config.bands) {
            Product srcProduct = getContext().getSourceProduct(bandDesc.product);
            if (StringUtils.isNotNullAndNotEmpty(bandDesc.name)) {
                if (StringUtils.isNotNullAndNotEmpty(bandDesc.newName)) {
                    copyBandWithFeatures(srcProduct, outputProduct, bandDesc.name, bandDesc.newName);
                } else {
                    copyBandWithFeatures(srcProduct, outputProduct, bandDesc.name);
                }
                allSrcProducts.add(srcProduct);
            } else if (StringUtils.isNotNullAndNotEmpty(bandDesc.nameExp)) {
                Pattern pattern = Pattern.compile(bandDesc.nameExp);
                for (String bandName : srcProduct.getBandNames()) {
                    Matcher matcher = pattern.matcher(bandName);
                    if (matcher.matches()) {
                        copyBandWithFeatures(srcProduct, outputProduct, bandName);
                        allSrcProducts.add(srcProduct);
                    }
                }
            }
        }

        for (Product srcProduct : allSrcProducts) {
            ProductUtils.copyBitmaskDefsAndOverlays(srcProduct, outputProduct);
        }

        return outputProduct;
    }

    /**
     * Copies the tie point data, geocoding and the start and stop time.
     *
     * @param sourceProduct
     * @param destinationProduct
     */
    private static void copyBaseGeoInfo(Product sourceProduct,
                                        Product destinationProduct) {
        // copy all tie point grids to output product
        ProductUtils.copyTiePointGrids(sourceProduct, destinationProduct);
        // copy geo-coding to the output product
        ProductUtils.copyGeoCoding(sourceProduct, destinationProduct);
        destinationProduct.setStartTime(sourceProduct.getStartTime());
        destinationProduct.setEndTime(sourceProduct.getEndTime());
    }

    private void copyBandWithFeatures(Product srcProduct, Product outputProduct, String oldBandName, String newBandName) {
        Band destBand = copyBandWithFeatures(srcProduct, outputProduct, oldBandName);
        destBand.setName(newBandName);
    }

    /**
     * @param outputProduct
     * @param srcProduct
     * @param name
     */
    private Band copyBandWithFeatures(Product srcProduct, Product outputProduct, String bandName) {
        Band destBand = ProductUtils.copyBand(bandName, srcProduct, outputProduct);
        Band srcBand = srcProduct.getBand(bandName);
        bandMapping.put(destBand, srcBand);
        if (srcBand.getFlagCoding() != null) {
            FlagCoding srcFlagCoding = srcBand.getFlagCoding();
            ProductUtils.copyFlagCoding(srcFlagCoding, outputProduct);
            destBand.setFlagCoding(outputProduct.getFlagCoding(srcFlagCoding.getName()));
        }
        destBand.setNoDataValueUsed(srcBand.isNoDataValueUsed());
        destBand.setNoDataValue(srcBand.getNoDataValue());
        return destBand;
    }

    @Override
    public void dispose() {
        bandMapping.clear();
    }

    @Override
    public void computeBand(Raster targetRaster, ProgressMonitor pm) throws OperatorException {
    	Rectangle rectangle = targetRaster.getRectangle();
        Band sourceBand = bandMapping.get(targetRaster.getRasterDataNode());
        Raster sourceRaster = getRaster(sourceBand, rectangle);
        
        // copy, because the databuffer is for computeAllBands not correctly (re)used
        final int length = rectangle.width * rectangle.height;
        System.arraycopy(sourceRaster.getDataBuffer().getElems(), 0, targetRaster.getDataBuffer().getElems(), 0, length);
    }


    public class BandDesc {
        String product;
        String name;
        String nameExp;
        String newName;
    }


    public static class Spi extends AbstractOperatorSpi {
        public Spi() {
            super(ProductMergeOp.class, "ProductMerger");
        }
    }
}