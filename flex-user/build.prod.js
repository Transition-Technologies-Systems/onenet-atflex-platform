var replace = require('replace-in-file');
var utils = require('./build.utils.js');
var pom = utils.parsePOM();

require('./build.js');

utils.replaceValue('environments/build.ts', /version: (.*)/g, 'version: \'' + pom.version + '\',');
utils.replaceValue('environments/build.ts', /timestamp: (.*)/g, 'timestamp: \'' + new Date().getTime() + '\',');
