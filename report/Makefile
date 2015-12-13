SOURCE=$(wildcard *.Rmd)
TARGET=$(SOURCE:.Rmd=.html)

run: all

%.html: %.Rmd data/benchmark.data
	scripts/knit.R $<

all: $(TARGET)
	# osascript -e 'tell application "Google Chrome" to reload active tab of window 1'

clean:
	rm *.html *.md

upload: all
	scp overview.html ts:www-truffle/are-we-fast-yet.html
