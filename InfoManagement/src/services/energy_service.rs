use actix_web::web;
use diesel::prelude::*;
use uuid::Uuid;
use crate::{db, db::pool::DbPool, models::energy::Energy, schema::energy};

pub async fn create_energy(
    pool: web::Data<DbPool>,
    energy_data: Energy,
) -> Option<usize> {
    let mut conn = db::get_connection(&pool);

    diesel::insert_into(energy::table)
        .values(&energy_data)
        .execute(&mut conn)
        .optional()
        .expect("Failed to insert into DB table")
}

pub async fn get_energies(
    pool: web::Data<DbPool>,
    wanted_device_id: Uuid,
    offset: i64,
) -> Option<Vec<Energy>> {
    let mut conn = db::get_connection(&pool);

    energy::table
        .filter(energy::device_id.eq(wanted_device_id))
        .offset(offset)
        .limit(20)
        .load::<Energy>(&mut conn)
        .optional()
        .expect("Failed to load DB table")
}

pub async fn read_energy(
    pool: web::Data<DbPool>,
    energy_id: Uuid,
) -> Option<Energy> {
    let mut conn = db::get_connection(&pool);

    energy::table
        .find(energy_id)
        .first::<Energy>(&mut conn)
        .optional()
        .expect("Failed to load DB row")
}