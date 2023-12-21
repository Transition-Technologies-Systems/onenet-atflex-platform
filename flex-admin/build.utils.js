var fs = require('fs');
var replace = require('replace-in-file');
var parseString = require('xml2js').parseString;

var defaultRootPath = 'src/main/webapp/';

function setBaseHref(href) {
  var baseHref = href ? href : '/';

  try {
    replace.sync({
      files: defaultRootPath + 'index.html',
      from: [/<base href="(.*)"/g],
      to: ['<base href="' + baseHref + '"'],
      allowEmptyPaths: false
    });
  } catch (error) {
    console.error('Error setBaseHref:', error);
    throw error;
  }
}

function replaceValue(files, from, to, customRootPath) {
  var rootPath = customRootPath !== undefined ? customRootPath : defaultRootPath;

  try {
    replace.sync({
      files: rootPath + files,
      from: from,
      to: to,
      allowEmptyPaths: false
    });
  } catch (error) {
    console.error('Error replaceValue:', error);
    throw error;
  }
}

function parsePOM() {
  var version = null;

  var pomXml = fs.readFileSync('pom.xml', 'utf8');

  parseString(pomXml, (err, result) => {
    if (err) {
      throw new Error('Failed to parse pom.xml: ' + err);
    }

    if (result.project.version && result.project.version[0]) {
      version = result.project.version[0];
    } else if (result.project.parent && result.project.parent[0] && result.project.parent[0].version && result.project.parent[0].version[0]) {
      version = result.project.parent[0].version[0];
    }
  });

  return {
    version: version
  };
}

module.exports = {
  setBaseHref: setBaseHref,
  replaceValue: replaceValue,
  parsePOM: parsePOM
};
