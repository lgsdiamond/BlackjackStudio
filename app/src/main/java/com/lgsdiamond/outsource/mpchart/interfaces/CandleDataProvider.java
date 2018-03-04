package com.lgsdiamond.outsource.mpchart.interfaces;

import com.lgsdiamond.outsource.mpchart.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}
