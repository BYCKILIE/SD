use actix_web::{get, post, web, HttpResponse};
use uuid::Uuid;
use crate::db::pool::DbPool;
use crate::models::energy::Energy;
use crate::services::energy_service::*;

// cursor
#[post("/create/energy")]
pub async fn create_energy_handler(
    pool: web::Data<DbPool>,
    energy_json: web::Json<Energy>,
) -> HttpResponse {
    match create_energy(pool, energy_json.into_inner()).await {
        Some(_) => HttpResponse::Ok().finish(),
        None => {
            HttpResponse::InternalServerError().finish()
        }
    }
}

#[get("/read/energy/{id}")]
pub async fn read_energy_handler(
    pool: web::Data<DbPool>,
    id: web::Path<String>,
) -> HttpResponse {
    match Uuid::parse_str(&id.into_inner()) {
        Ok(wanted_id) =>
            match read_energy(pool, wanted_id).await {
                Some(device) => HttpResponse::Ok().json(device),
                None => HttpResponse::InternalServerError().finish(),
            }
        Err(_) =>
            HttpResponse::InternalServerError().finish(),
    }
}

#[get("/get/energy/{id}/{offset}")]
pub async fn get_energy_handler(
    pool: web::Data<DbPool>,
    id: web::Path<String>,
    offset: web::Path<i64>,
) -> HttpResponse {
    match Uuid::parse_str(&id.into_inner()) {
        Ok(wanted_id) =>
            match get_energies(pool, wanted_id, offset.into_inner()).await {
                Some(device) => HttpResponse::Ok().json(device),
                None => HttpResponse::InternalServerError().finish(),
            }
        Err(_) =>
            HttpResponse::InternalServerError().finish(),
    }
}

pub fn energy_routes(cfg: &mut web::ServiceConfig) {
    cfg
        .service(create_energy_handler)
        .service(read_energy_handler);

}