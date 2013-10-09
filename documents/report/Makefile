NAME=report

all:
	pdflatex $(NAME)
	bibtex $(NAME)
	pdflatex $(NAME)

clean:
	rm -f \
		*.aux \
		*.bbl \
		$(NAME).log *.blg \
		$(NAME).out \
		$(NAME).toc \
		$(NAME).pdf
