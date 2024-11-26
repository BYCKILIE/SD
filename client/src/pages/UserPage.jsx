import {useEffect, useState} from 'react';
import useAuth from '../hooks/useAuth.js';
import {useNavigate} from "react-router-dom";
import "../styles/home.css";
import MenuForm from "../forms/MenuForm.jsx";
import useProfiles from "../hooks/userProfiles.js";
import {useQuery} from "../hooks/useQuery.js";

const headers = [
    'Devices',
    'Map',
    'Edit'
];

const UserPage = () => {
    const query = useQuery()
    const email = query.get("email");

    const [activeTab, setActiveTab] = useState(0);

    const [names, setNames] = useState("");

    const {adminAuth, admin} = useAuth();
    const {readAdminProfile} = useProfiles();
    const navigate = useNavigate();

    useEffect(() => {
        const handleAuth = async () => {
            await adminAuth();
            if (!admin) {
                navigate("/login")
                return
            }
            const names = await readAdminProfile(email);
            setNames(names);
        }
        handleAuth();
    }, []);

    const changeTabOnClick = async (index) => {
        setActiveTab(index);
    };

    return (
        <MenuForm names={names} headers={headers} activeTab={activeTab} changeTabOnClick={changeTabOnClick} greeting={"Client"}
                  mode={"adminUser"}/>
    );
};

export default UserPage;
