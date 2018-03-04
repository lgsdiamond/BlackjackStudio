package com.lgsdiamond.outsource.mpchart.formatter;

import com.lgsdiamond.outsource.mpchart.data.LineDataSet;
import com.lgsdiamond.outsource.mpchart.interfaces.LineDataProvider;

/**
 * Interface for providing a custom logic to where the filling line of a LineDataSet
 * should end. This of course only works if setFillEnabled(...) is set to true.
 * 
 * @author Philipp Jahoda
 */
public interface FillFormatter {

    /**
     * Returns the vertical (y-axis) position where the filled-line of the
     * LineDataSet should end.
     * 
     * @param dataSet the LineDataSet that is currently drawn
     * @param dataProvider
     * @return
     */
    float getFillLinePosition(LineDataSet dataSet, LineDataProvider dataProvider);
}
