// @generated automatically by Diesel CLI.

diesel::table! {
    devices (id) {
        id -> Uuid,
        value -> Float8,
    }
}

diesel::table! {
    energy (id) {
        id -> Uuid,
        device_id -> Nullable<Uuid>,
        current -> Float8,
        created_at -> Nullable<Timestamp>,
    }
}

diesel::joinable!(energy -> devices (device_id));

diesel::allow_tables_to_appear_in_same_query!(
    devices,
    energy,
);
