package com.lgsdiamond.outsource.mpchart.interfaces;

import com.lgsdiamond.outsource.mpchart.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}
