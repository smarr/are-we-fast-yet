SOURCE=$(wildcard *.Rmd)
TARGET=$(SOURCE:.Rmd=.html)

run: all

%.html: %.Rmd data/benchmark.data.bz2
	scripts/knit.R $<

data/benchmark.data.bz2:
	mkdir -p data
	scripts/get-data.sh

all: $(TARGET)
	# osascript -e 'tell application "Google Chrome" to reload active tab of window 1'

clean:
	rm *.html *.md

clobber: clean
	rm -Rf data/benchmark.data

upload: all
	scp overview.html ts:www-truffle/are-we-fast-yet.html
	echo "Report Available at http://stefan-marr.de/downloads/truffle/are-we-fast-yet.html"

docs: overview.html
	cp overview.md ../docs/performance.md
	cp figures/*.png ../docs/figures/
