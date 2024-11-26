use diesel::{Queryable, Insertable, AsChangeset};
use serde::{Deserialize, Serialize};
use uuid::Uuid;
use crate::schema::devices;

#[derive(AsChangeset, Queryable, Insertable, Serialize, Deserialize)]
#[diesel(table_name = devices)]
pub struct Device {
    pub id: Uuid,
    pub value: f64,
}