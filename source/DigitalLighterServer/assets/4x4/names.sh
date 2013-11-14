#!/bin/bash

for i in *; do
	if [ -d $i ]; then
		if [ $i != "equalizer4x4" -a $i != "expand" ]; then
			cd $i			
			frame=`ls`
							
			for j in {1..10}; do
				cp $frame ${frame%\.png}${j}.png
#				echo ${frame%\.png}${j}.png
			done			

			mv $frame ${frame%\.png}0.png
			cd ..
		fi
	fi
done
