package com.lgsdiamond.outsource.mpchart.interfaces;

import com.lgsdiamond.outsource.mpchart.components.YAxis.AxisDependency;
import com.lgsdiamond.outsource.mpchart.data.BarLineScatterCandleBubbleData;
import com.lgsdiamond.outsource.mpchart.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    int getMaxVisibleCount();
    boolean isInverted(AxisDependency axis);
    
    int getLowestVisibleXIndex();
    int getHighestVisibleXIndex();

    BarLineScatterCandleBubbleData getData();
}
