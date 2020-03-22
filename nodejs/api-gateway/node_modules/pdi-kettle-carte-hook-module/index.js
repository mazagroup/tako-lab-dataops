const parser = require('xml2json')
const zlib = require('zlib')
const pump = require('pump')
const toArray = require('stream-to-array')

exports.transStatusXml2JSON = async ( req, res, stream ) => {
    try {
        const resBuffer = Buffer.concat(await toArray(stream))
        const xml = resBuffer.toString('utf8')
        const json = parser.toJson(xml)

        var json_object = JSON.parse(json)
        const trans_log_zipped = json_object.transstatus.logging_string.replace("<![CDATA[", "").replace("]]>", "")
        const trans_log_zipped_buffer = Buffer.from(trans_log_zipped, 'base64')

        zlib.unzip(trans_log_zipped_buffer, (err, trans_log_zipped_buffer) => {
        if (!err) {
            console.log(trans_log_zipped_buffer.toString());
            json_object.transstatus.logging_string = trans_log_zipped_buffer.toString()

            const jsonBuffer = Buffer.from(JSON.stringify(json_object), 'utf8')
            res.statusCode = stream.statusCode
            res.setHeader('content-length', '' + Buffer.byteLength(jsonBuffer))
            res.setHeader('content-type', 'application/json')
            res.end(jsonBuffer)
        } else {
            // handle error
            res.statusCode = stream.statusCode
            pump(stream, res)
        }
        })
    } catch (err) {
        res.statusCode = 500 
        pump(stream, res)
    }
}

exports.defaultXml2JSON = async ( req, res, stream ) => {
    try {
        const resBuffer = Buffer.concat(await toArray(stream))
        const xml = resBuffer.toString('utf8')
        const json = parser.toJson(xml)

        const jsonBuffer = Buffer.from(json, 'utf8')
        res.statusCode = stream.statusCode
        res.setHeader('content-length', '' + Buffer.byteLength(jsonBuffer))
        res.setHeader('content-type', 'application/json')
        res.end(jsonBuffer)
    } catch (err) {
        res.statusCode = 500 
        pump(stream, res)
    }
}

exports.defaultRequest = async ( req, res, stream ) => {
    try {
        const resBuffer = Buffer.concat(await toArray(stream))
  
        res.statusCode = stream.statusCode
        res.setHeader('content-length', '' + Buffer.byteLength(resBuffer))
        res.end(resBuffer)
    } catch (err) {
        res.statusCode = 500 
        pump(stream, res)
    }
}