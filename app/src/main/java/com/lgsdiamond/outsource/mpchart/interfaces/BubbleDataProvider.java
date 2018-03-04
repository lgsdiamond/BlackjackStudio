package com.lgsdiamond.outsource.mpchart.interfaces;

import com.lgsdiamond.outsource.mpchart.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}
