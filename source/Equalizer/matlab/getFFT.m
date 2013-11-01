function bandValues = getFFT(bands)
    bandValues  = rand(1, bands);
    d           = randi(10) - 1;
    
    bandValues(1) = bandValues(1) * 0.2 + (d / 10) * 0.8;   
    if(bands > 1)
        bandValues(2) = bandValues(2) * 0.3 + (d / 10) * 0.7;
    end
    if(bands > 1)
        bandValues(3) = bandValues(3) * 0.5 + (d / 10) * 0.5;
    end    
end