#!/usr/bin/env bash
export SDL_VIDEODRIVER=dummy
exec `dirname $0`/$SCRIPT_PATH/RSqueak/rsqueak $@
