/*
 * $Id: BandInfo.java,v 1.1 2006/09/18 06:34:32 marcop Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.dataio.envisat;

import org.esa.beam.framework.datamodel.FlagCoding;

/**
 * The <code>BandInfo</code> class provides information for an available band within an ENVISAT product.
 * <p/>
 * <p> Note that this class is public only as a side-effect of the implementation.
 *
 * @author Norman Fomferra
 * @version $Revision: 1.1 $ $Date: 2006/09/18 06:34:32 $
 */
public class BandInfo extends DataItemInfo {

    public final static int SCALE_NONE = 10;
    public final static int SCALE_LINEAR = 11;
    public final static int SCALE_LOG10 = 12;

    public final static int SMODEL_1OF1 = 20;
    public final static int SMODEL_1OF2 = 21;
    public final static int SMODEL_2OF2 = 22;
    public final static int SMODEL_2UB_TO_S = 23;
    public final static int SMODEL_3UB_TO_I = 24;

    /**
     * The (zero-based)  spectral band index.
     */
    private int _spectralBandIndex;

    /**
     * The sample model operation
     */
    private int _sampleModel;

    /**
     * The scaling method
     */
    private int _scalingMethod;

    /**
     * The scaling offset
     */
    private float _scalingOffset;

    /**
     * The scaling factor
     */
    private float _scalingFactor;

    /**
     * Optional bit-mask expression
     */
    private String _validExpression;

    /**
     * Optional flag-coding (for flag datasets only)
     */
    private FlagCoding _flagCoding;

    /**
     * The width of the band
     */
    private int _bandWidth;

    /**
     * The height of the band
     */
    private int _bandHeight;

    /**
     * Constructs a new band information object.
     */
    public BandInfo(String bandName,
                    int dataType,
                    int spectralBandIndex,
                    int sampleModel,
                    int scalingMethod,
                    float scalingOffset,
                    float scalingFactor,
                    String validExpression,
                    FlagCoding flagCoding,
                    String physicalUnit,
                    String description,
                    int width,
                    int height) {
        super(bandName, dataType, physicalUnit, description);
        _spectralBandIndex = spectralBandIndex;
        _sampleModel = sampleModel;
        _scalingMethod = scalingMethod;
        _scalingOffset = scalingOffset;
        _scalingFactor = scalingFactor;
        _validExpression = validExpression;
        _flagCoding = flagCoding;
        _bandWidth = width;
        _bandHeight = height;
    }

    /**
     * Returns the (zero-based) spectral band index.
     *
     * @return the (zero-based) spectral band index.
     */
    public int getSpectralBandIndex() {
        return _spectralBandIndex;
    }

    /**
     * Returns the sample model.
     *
     * @return the sample model, always one of the <code>SMODEL_</code>XXX constants defined in this class
     */
    public final int getSampleModel() {
        return _sampleModel;
    }

    /**
     * Returns the scaling method.
     *
     * @return the sample model, always one of the <code>SCALE_</code>XXX constants defined in this class
     */
    public final int getScalingMethod() {
        return _scalingMethod;
    }

    /**
     * Returns the scaling offset (or interception).
     */
    public final float getScalingOffset() {
        return _scalingOffset;
    }

    /**
     * Returns the scaling factor.
     */
    public final float getScalingFactor() {
        return _scalingFactor;
    }

    /**
     * Returns the optional bit-mask expression.
     */
    public String getValidExpression() {
        return _validExpression;
    }

    /**
     * Returns the optional flag-coding (for flag datasets only).
     */
    public FlagCoding getFlagCoding() {
        return _flagCoding;
    }

    /**
     * Returns the width of the band.
     */
    public int getWidth() {
        return _bandWidth;
    }

    /**
     * Returns the height of the band.
     */
    public int getHeight() {
        return _bandHeight;
    }
}
