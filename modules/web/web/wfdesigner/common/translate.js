/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/*
 * Helper functions
 */
function stripStr(str) {
    return str.replace(/^\s*/, "").replace(/\s*$/, "");
}

// Multiline strip
function stripStrML(str) {
    // Split because m flag doesn't exist before JS1.5 and we need to
    // strip newlines anyway
    var parts = str.split('\n');
    for (var i = 0; i < parts.length; i++)
        parts[i] = stripStr(parts[i]);

    // Don't join with empty strings, because it "concats" words
    // And strip again
    return stripStr(parts.join(" "));
}

/*
 * C-printf like function, which substitutes %s with parameters
 * given in list. %%s is used to escape %s.
 *
 * Doesn't work in IE5.0 (splice)
 */
function printf(S, L) {
    if (!L) return S;

    var nS = "";
    var tS = S.split("%s");

    for (var i = 0; i < L.length; i++) {
        if (tS[i].lastIndexOf('%') == tS[i].length - 1 && i != L.length - 1)
            tS[i] += "s" + tS.splice(i + 1, 1)[0];
        nS += tS[i] + L[i];
    }
    return nS + tS[tS.length - 1];
}

function removeHTMLTags(strInputCode) {
    /*
     This line is optional, it replaces escaped brackets with real ones,
     i.e. < is replaced with < and > is replaced with >
     */
    strInputCode = strInputCode.replace(/&(lt|gt);/g, function (strMatch, p1){
        return (p1 == "lt")? "<" : ">";
    });
    return strInputCode.replace(/<\/?[^>]+(>|$)/g, "");
}

/*
 * i18n
 */
function I18n(i18n_dict) {
    this.i18nd = i18n_dict;

    // Change to entity representation non-ASCII characters
    this.toEntity = function (str) {
        var newstr = "";
        for (var i = 0; i < str.length; i++) {
            if (str.charCodeAt(i) > 128)
                newstr += "&#" + str.charCodeAt(i) + ";";
            else
                newstr += str.charAt(i);
        }
        return newstr;
    };

    // Return translation, if translation dictionary exists and has a translation.
    this.get = function (str, params) {
        var transl = str;
        if (this.i18nd && this.i18nd[str])
            transl = this.i18nd[str];
        return printf(transl, params);
    };

    this.translateNodes = function () {

        function doTranslate(self, e) {
            var innerText = removeHTMLTags(e.innerHTML);
            var orig = stripStrML(innerText);
            var transl = self.get(orig);
            if (transl != orig)
                e.innerHTML = e.innerHTML.replace(orig, transl);

            // If translation not found, try again with
            // entity representation
            //if (transl == orig) transl = self.translate(self.toEntity(orig));
            //e.innerHTML = transl;
        }

        function dive(self, n) {
            if (n.nodeType == 1) {
                doTranslate(self, n);
                var children = n.childNodes;
                for (var i = 0; i < children.length; i++) {
                    dive(self, children[i]);
                }
            }
        }

        var nodes = YAHOO.util.Dom.getElementsByClassName("i18n");
        for (var i = 0; i < nodes.length; i++) {
            dive(this, nodes[i]);
        }
    };
}
