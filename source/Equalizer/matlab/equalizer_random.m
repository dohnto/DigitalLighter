                        %%% Equalizer Random %%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% SET PARAMETERS

outDir  = 'equalizer5x4';
fps     = 25;
length  = 10;   % seconds

tilesX  = 5;
tilesY  = 4;

for gg = 5:7
    tilesX = gg;
    
    for hh = 4:7
        tilesY = hh;        
        outDir = strcat('equalizer', num2str(tilesX), 'x', num2str(tilesY));
        
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% GLOBALS

bandValues      = zeros(1, tilesX);
oldBandValues   = zeros(1, tilesX);
volume          = 1;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% INIT

oldBandValues = getFFT(tilesX);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% RUN

eq = zeros(tilesX, fps*length);

for ii = 1:length*fps    
    bandValues = getFFT(tilesX);
    
    % for each band count new amplitude
    for bandIdx = 1:tilesX
        [oldBandValues eq(bandIdx, ii)] = calcBand(oldBandValues, bandValues, bandIdx, volume);
    end   
end

% map values of eq into scale <0; tilesY>
eq = fix(eq * (tilesY + 1));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% CREATE OUTPUT PNG PICTURES

% generate colors for amplitude levels
colors      = zeros(3, tilesY);  % rows = RGB
colorStep   = fix(512 / (tilesY - 1));

for ii = 0:(tilesY - 1)
    colors(1, ii + 1) = ii * colorStep;
    colors(2, ii + 1) = (tilesY - 1 - ii) * colorStep; 
end

colors(colors > 255) = 255;

% frame by frame
for ii = 1:fps*length
    frame = zeros(tilesY, tilesX, 3);
    
    for jj = 1:tilesX
        for kk = 1:eq(jj, ii)
            frame(tilesY - kk + 1, jj, 1) = colors(1, kk);
            frame(tilesY - kk + 1, jj, 2) = colors(2, kk);
        end
    end
    
    frame = uint8(frame);    
    
    imwrite(frame, strcat(outDir, '\', 'f', sprintf('%05d', ii), '.png'));
end
    end
end

