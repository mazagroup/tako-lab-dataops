const dotenv = require('dotenv');
dotenv.config();
module.exports = {
    tako_etl_pdi_carte_admin_user: process.env.TAKO_ETL_PDI_CARTE_ADMIN_USER,
    tako_etl_pdi_carte_admin_pass: process.env.TAKO_ETL_PDI_CARTE_ADMIN_PASS,
    tako_etl_pdi_carte_host_name: process.env.TAKO_ETL_PDI_CARTE_HOST_NAME,
    tako_etl_pdi_carte_host_port: process.env.TAKO_ETL_PDI_CARTE_HOST_PORT,
    tako_etl_pdi_carte_host_protocol: process.env.TAKO_ETL_PDI_CARTE_HOST_PROTOCOL,
    tako_etl_pdi_carte_cxf_host_protocol: process.env.TAKO_ETL_PDI_CARTE_CXF_HOST_PROTOCOL,
    tako_etl_pdi_carte_cxf_host_port: process.env.TAKO_ETL_PDI_CARTE_CXF_HOST_PORT,
    tako_api_gateway_port: process.env.TAKO_API_GATEWAY_PORT
  };