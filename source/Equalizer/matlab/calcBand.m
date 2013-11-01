function [oldBandValuesNext amplitude] = calcBand(oldBandValues, bandValues, bandIdx, volume)
    oldBandValuesNext = oldBandValues;

    bv  = bandValues(bandIdx);
    obv = oldBandValues(bandIdx);
    
    if bv >= obv
        obv = bv;
    end
    
    obv = obv - 0.1;
	obv = obv * volume;
	
    if obv < 0
        obv = 0;
    end

	oldBandValuesNext(bandIdx) = obv;
   
	amplitude = obv;
end