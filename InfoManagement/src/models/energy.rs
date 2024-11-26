use diesel::{Queryable, Insertable, AsChangeset};
use serde::{Deserialize, Serialize};
use uuid::Uuid;
use chrono::NaiveDateTime;
use crate::schema::energy;


#[derive(AsChangeset, Queryable, Insertable, Serialize, Deserialize)]
#[diesel(table_name = energy)]
pub struct Energy {
    pub id: Uuid,
    pub device_id: Option<Uuid>,
    pub current: f64,
    pub created_at: Option<NaiveDateTime>,
}