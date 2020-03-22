const parser = require('xml2json')
const zlib = require('zlib')
const pump = require('pump')
const toArray = require('stream-to-array')
const gateway = require('fast-gateway')

const carteHook = require('pdi-kettle-carte-hook-module')
const config = require('./config')

const carte_admin_username = config.tako_etl_pdi_carte_admin_user;
const carte_admin_password = config.tako_etl_pdi_carte_admin_pass;
const carte_auth = 'Basic ' + Buffer.from(carte_admin_username + ':' + carte_admin_password).toString('base64')

const server = gateway({
  routes: [{
    prefix: '/pdi/carte',
    target: config.tako_etl_pdi_carte_host_protocol+'://'+config.tako_etl_pdi_carte_host_name+':'+config.tako_etl_pdi_carte_host_port,
    hooks: {
      async onRequest(req, res) {
        req.headers['Authorization']=carte_auth
      },
      async onResponse(req, res, stream) {
        if (req.path.startsWith('/pdi/carte/kettle/transStatus')) {
          carteHook.transStatusXml2JSON( req, res, stream )    
        } 
        else if (req.path.includes('Image')) { 
          carteHook.defaultRequest( req, res, stream )
        }
        else {
          carteHook.defaultXml2JSON( req, res, stream )
        }
      }
    }
  },
  {
    prefix: '/pdi/ws',
    target: config.tako_etl_pdi_carte_cxf_host_protocol+'://'+config.tako_etl_pdi_carte_host_name+':'+config.tako_etl_pdi_carte_cxf_host_port,
    hooks: {
      async onRequest(req, res) {
        req.headers['Authorization']=carte_auth
        req.url = '/cxf/pdi/ws'+req.url
      },
      async onResponse(req, res, stream) {
        try {
            const resBuffer = Buffer.concat(await toArray(stream))
      
            res.statusCode = stream.statusCode
            res.setHeader('content-length', '' + Buffer.byteLength(resBuffer))
            res.setHeader('content-type', 'application/json')
            res.end(resBuffer)
        } catch (err) {
            res.statusCode = 500 
            pump(stream, res)
        }
      }
    }
  }
]
})
server.start( config.tako_api_gateway_port )
console.log('API Gateway running on port '+config.tako_api_gateway_port);