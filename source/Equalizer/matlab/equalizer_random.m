                        %%% Equalizer Random %%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% SET PARAMETERS

outDir  = 'out';
fps     = 25;
length  = 20;   % seconds

tilesX  = 4;
tilesY  = 5;

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
    
    imwrite(frame, strcat(outDir, '\', 'f', num2str(ii), '.png'));
end