/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.core.features.fd;

import java.awt.Polygon;

import org.knime.knip.core.features.FeatureSet;
import org.knime.knip.core.features.FeatureTargetListener;
import org.knime.knip.core.features.ObjectCalcAndCache;
import org.knime.knip.core.features.SharesObjects;
import org.knime.knip.core.util.PolygonTools;

import net.imglib2.IterableInterval;
import net.imglib2.type.logic.BitType;

/**
 * Calculates the distance of the points on the contour to the centroid.
 *
 * The contour points are extracted by an contour tracing algorithm (
 * {@link PolygonTools#extractPolygon(net.imglib2.RandomAccessibleInterval, int[])}).
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class CentroidDistanceFeatureSet implements FeatureSet, SharesObjects {

    private double[] m_distances;

    private ObjectCalcAndCache m_ocac;

    /**
     * @param numPoints
     * @param target
     */
    public CentroidDistanceFeatureSet(final int numPoints) {
        m_distances = new double[numPoints];
    }

    @FeatureTargetListener
    public void iiUpdated(final IterableInterval<BitType> interval) {
        Polygon poly = m_ocac.traceContour(interval);
        final double[] centroid = m_ocac.centroid(interval);
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] -= interval.min(i);
        }
        double tmpx, tmpy;
        for (int x = 0; x < m_distances.length; x++) {
            int i = (int)((poly.npoints / (double)m_distances.length) * x);
            tmpx = poly.xpoints[i] - centroid[0];
            tmpy = poly.ypoints[i] - centroid[1];

            m_distances[x] = Math.sqrt(tmpx * tmpx + tmpy * tmpy);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double value(final int id) {
        return m_distances[id];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name(final int id) {
        return "CentroidDistance [" + id + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numFeatures() {
        return m_distances.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String featureSetId() {
        return "Centroid Distance Feature Factory";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enable(final int id) {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getSharedObjectClasses() {
        return new Class[]{ObjectCalcAndCache.class};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharedObjectInstances(final Object[] instances) {
        m_ocac = (ObjectCalcAndCache)instances[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUp() {
        m_ocac.cleanUp();
    }


}
