mod devices_routes;
mod energy_routes;

use actix_web::web;

pub fn init_routes(cfg: &mut web::ServiceConfig) {
    devices_routes::devices_routes(cfg);
    energy_routes::energy_routes(cfg);
}