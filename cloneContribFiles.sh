if [ "$2" = "yes" ]
    then
        git clone -q "$1" campusmap
        WORKDIR=`pwd`
        cd campusmap
        git reset --hard "$3"
        cd "$WORKDIR"
        mv campusmap/extra-data.yaml "$WORKDIR"/extra-data.yaml
        rm -rf campusmap/
fi
