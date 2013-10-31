                            %%% Equalizer %%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% SET PARAMETERS

song    = 'piano44khz.wav';
outDir  = 'out';
fps     = 25;

tilesX  = 4;
tilesY  = 5;

fLow    = 40;       % Hz
fHigh   = 14000;    % Hz

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% INFERED PARAMETERS

tones           = fix(log10(fHigh / fLow) / log10(2) * 12);
tonesPerBand    = fix(tones / tilesX);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% LOAD SOUND

[s, Fs] = wavread(song);
s       = sum(s,2)/2;   % two channels to one

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% CREATE SPECTROGRAM ACCORDING TO GIVEN PARAMETERS

window  = Fs/fps;
nfft    = window;
step    = Fs/nfft; 

sgram   = spectrogram(s, window, 0, nfft, Fs);
sgram   = abs(sgram);

% plot spectrogram (x - time, y - frequency)
figure(1);
imagesc((0:fix(length(s)/window))/fps, (0:(window/2))*Fs/window, sgram)


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% QUANTIZE

% prepare bands ranges
bands       = zeros(1, tilesX + 1);
bands(1)    = fLow;

for ii = 2:(tilesX + 1)
    bands(ii) = bands(ii-1) * 2^(tonesPerBand / 12);
end

% quantize frequencies
equ     = zeros(tilesX, size(sgram,2));

for ii = 1:tilesX
    idxLow  = fix(bands(ii)     / step); 
    idxHigh = fix(bands(ii + 1) / step);
    
    equ(ii,:) = sum(sgram(idxLow:idxHigh,:));
end

% scale amplitudes according to human ear perception (logarithm)
equ = log10(equ);

% quantize amplitudes
ampMax  = max(max(equ));
ampBand = ampMax / (tilesY + 1);
equ     = fix(equ / ampBand);

% display quantized spectrogram
figure(2);
imagesc((0:fix(length(s)/window))/fps, (0:(tilesX))*Fs/2/tilesX, equ)

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
for ii = 1:size(sgram,2)
    frame = zeros(tilesY, tilesX, 3);
    
    for jj = 1:tilesX
        for kk = 1:equ(jj, ii)
            frame(tilesY - kk + 1, jj, 1) = colors(1, kk);
            frame(tilesY - kk + 1, jj, 2) = colors(2, kk);
        end
    end
    
    frame = uint8(frame);
    
    imwrite(frame, strcat(outDir, '\', 'f', num2str(ii), '.png'));
end



%%% DEBUG

% soundsc(s, Fs)

