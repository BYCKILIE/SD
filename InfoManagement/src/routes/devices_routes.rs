use actix_web::{get, post, web, HttpResponse};
use uuid::Uuid;
use crate::db::pool::DbPool;
use crate::models::device::Device;
use crate::services::devices_service::*;

#[post("/create/device")]
pub async fn create_device_handler(
    pool: web::Data<DbPool>,
    device: web::Json<Device>,
) -> HttpResponse {
    match create_device(pool, device.into_inner()).await {
        Some(_) => HttpResponse::Ok().finish(),
        None => {
            eprintln!("Error creating device");
            HttpResponse::InternalServerError().finish()
        }
    }
}

#[get("/read/device/{id}")]
pub async fn read_device_handler(
    pool: web::Data<DbPool>,
    id: web::Path<String>,
) -> HttpResponse {
    match Uuid::parse_str(&id.into_inner()) {
        Ok(wanted_id) =>
            match read_device(pool, wanted_id).await {
                Some(device) => HttpResponse::Ok().json(device),
                None => HttpResponse::NotFound().json(serde_json::json!({
            "error": "Device not found"
        })),
            }
        Err(_) => HttpResponse::NotFound().json(serde_json::json!({
            "error": "Device not found"
        }))
    }
}

#[post("/update/device/{id}")]
pub async fn update_device_handler(
    pool: web::Data<DbPool>,
    id: web::Path<String>,
    updated_device: web::Json<Device>,
) -> HttpResponse {
    match Uuid::parse_str(&id.into_inner()) {
        Ok(wanted_id) =>
            match update_device(pool, wanted_id, updated_device).await {
                Some(_) => HttpResponse::Ok().finish(),
                None => HttpResponse::InternalServerError().finish(),
            }
        Err(_) => HttpResponse::NotFound().json(serde_json::json!({
            "error": "Device not found"
        }))
    }
}

#[get("/delete/device/{id}")]
pub async fn delete_device_handler(
    pool: web::Data<DbPool>,
    id: web::Path<String>,
) -> HttpResponse {
    match Uuid::parse_str(&id.into_inner()) {
        Ok(wanted_id) =>
            match delete_device(pool, wanted_id).await {
            Some(_) => HttpResponse::Ok().finish(),
            None => HttpResponse::InternalServerError().finish(),
        }
        Err(_) => HttpResponse::NotFound().json(serde_json::json!({
            "error": "Device not found"
        }))    }
}

pub fn devices_routes(cfg: &mut web::ServiceConfig) {
    cfg
        .service(create_device_handler)
        .service(read_device_handler)
        .service(update_device_handler)
        .service(delete_device_handler);
}