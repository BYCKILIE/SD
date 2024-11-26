use actix_web::web;
use diesel::prelude::*;
use uuid::Uuid;
use crate::{db, db::pool::DbPool, models::device::Device, schema::devices};

pub async fn create_device(
    pool: web::Data<DbPool>,
    device: Device,
) -> Option<usize> {
    let mut conn = db::get_connection(&pool);

    diesel::insert_into(devices::table)
        .values(&device)
        .execute(&mut conn)
        .optional()
        .expect("Error creating device")
}

pub async fn read_device(
    pool: web::Data<DbPool>,
    device_id: Uuid,
) -> Option<Device> {
    let mut conn = db::get_connection(&pool);

    devices::table
        .find(device_id)
        .first::<Device>(&mut conn)
        .optional()
        .expect("Error loading device")
}

pub async fn update_device(
    pool: web::Data<DbPool>,
    device_id: Uuid,
    updated_device: web::Json<Device>,
) -> Option<usize> {
    let mut conn = db::get_connection(&pool);

    diesel::update(devices::table.find(device_id))
        .set(&*updated_device)
        .execute(&mut conn)
        .optional()
        .expect("Error updating device")
}

pub async fn delete_device(
    pool: web::Data<DbPool>, device_id: Uuid,
) -> Option<usize> {
    let mut conn = db::get_connection(&pool);

    diesel::delete(devices::table.find(device_id))
        .execute(&mut conn)
        .optional()
        .expect("Error deleting device")
}
