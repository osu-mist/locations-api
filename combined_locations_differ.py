"""
    Usage:
    combined_locations_differ.py <old_data_path> <new_data_path>
    combined_locations_differ.py <old_data_path> <new_data_path> --coord_threshold=<threshold>

    Arguments:
        old_data_path: File path of old building json
        new_data_path: File path of new building json
    
    Options:
        --coord_threshold=<threshold>   Floating point difference threshold for reporting changed coordinate values
"""

import sys
import json
from dictdiffer import diff
import ndjson

from docopt import docopt
import pprint
#print(docopt(__doc__, version='1.0.0rc2'))

args = docopt(__doc__, version='1.0.0rc2')

def reportDiffsInCommonIDS(common_ids, old_mapped_locations, new_mapped_locations):
    print "\nReporting common ids diffs"
    print "{} ids have differences.".format(len(common_ids))
    print "\n====================================="
    for idx in common_ids:
        if(old_mapped_locations[idx] != new_mapped_locations[idx]):
            print 'Location ID {}'.format(idx)
            reportDiff(list(diff(old_mapped_locations[idx], new_mapped_locations[idx])))
            print "-------------------------------------"

def reportAddsAndRemoves(added_ids, removed_ids):
    print "====================================="
    print "\nNew or missing id's"
    print "# of new features: {}".format(len(added_ids))
    print "# of removed features: {}".format(len(removed_ids))
    if(added_ids):
        print "-------------------------------------"
        print "New features ids:"
        print added_ids
    if(removed_ids):
        print "-------------------------------------"
        print "Deprecated feature ids:"
        print removed_ids
    print "\n====================================="

"""
    Splits the dictdiffer diff by category: adds, removes, changes
"""
def splitDiffByCategory(diff):
    adds = {}
    removes = {}
    changes = {}
    #repr usage is to handle nested object diffs from throwing TypeError: unhashable type 'list'
    for d in diff:
        if(d[0] == 'change'):
            changes[d[1].__repr__()] = d[2]
        elif(d[0] == 'add'):
            adds[d[1].__repr__()] = d[2]
        elif(d[0] == 'remove'):
            removes[d[1].__repr__()] = d[2]
    return adds, removes, changes

'''
    Reporting for a dictdiffer diff
'''
def reportDiff(diff):
    adds, removes, changes = splitDiffByCategory(diff)
    if adds:
        print "Added Attributes:"
        pprint.pprint(adds)
    if removes:
        print "\nRemoved Attributes:"
        pprint.pprint(removes)
    if changes:
        print "\nChanged Atrributes:"
        for key, value in changes.iteritems():
            print "{}: `{}` -> `{}`".format(key, value[0], value[1])

"""
    Returns a JSON array that has been turned into a dict key'd by the key parameter value.
    Typically on each item's ['id'] field when used on JsonAPI speced Json.
    Used for diffing things based on id instead of position in an array.
"""
def mapJsonArrayToDict(objects, key='id'):
    mapped = {}
    for item in objects:
        mapped[item[key]] = item
    return mapped

def mapLocationsToIDS(old_locations, new_locations):
    return mapJsonArrayToDict(old_locations), mapJsonArrayToDict(new_locations)

if __name__ == "__main__":
    if(args['<old_data_path>'] == args['<new_data_path>']):
        print "These are the same file..."
        sys.exit(1)
    else:
        with open(args['<old_data_path>'], "r") as old_json_file, open(args['<new_data_path>'], "r") as new_json_file:
            
            old_locations = ndjson.loads(new_json_file.read())
            new_locations = ndjson.loads(old_json_file.read())

        if (old_locations == new_locations):
            # print "There are no differences"
            sys.exit(0)
        else:
            # print "There are differences"

            grab_ids = lambda objs : [x['id'] for x in objs]
            old_ids = set(grab_ids(old_locations))
            new_ids = set(grab_ids(new_locations))
            
            common_ids = old_ids & new_ids
            removed_ids = list(old_ids - common_ids)
            added_ids = list(new_ids - common_ids)

            reportAddsAndRemoves(added_ids, removed_ids)

            old_mapped_locations, new_mapped_locations = mapLocationsToIDS(old_locations, new_locations)
            
            reportDiffsInCommonIDS(common_ids, old_mapped_locations, new_mapped_locations)
