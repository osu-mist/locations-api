if [ "$2" = "yes" ]
    then
        git clone -q "$1" campusmap
        WORKDIR=`pwd`
        cd campusmap
        git reset --hard "$3"
        cd "$WORKDIR"
        mv campusmap/campusmap.json $WORKDIR/campusmap.json
        rm -rf campusmap/
fi