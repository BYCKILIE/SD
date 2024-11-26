import {useEffect, useState} from 'react';
import useAuth from '../hooks/useAuth.js';
import {useNavigate} from "react-router-dom";
import "../styles/home.css";
import useProfiles from "../hooks/userProfiles.js";
import MenuForm from "../forms/MenuForm.jsx";

const headers = [
    'Users',
    'Devices',
    'Create',
    'Monitoring',
    'Chat'
];

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState(0);

    const [names, setNames] = useState("");

    const {adminAuth, admin} = useAuth();
    const {readProfile} = useProfiles();
    const navigate = useNavigate();

    useEffect(() => {
        const handleAuth = async () => {
            await adminAuth();
            if (!admin) {
                navigate("/login");
                return;
            }
            const profileData = await readProfile();
            setNames(profileData);
        }

        handleAuth();
    }, []);

    const changeTabOnClick = async (index) => {
        setActiveTab(index);
    };

    return (
        <MenuForm names={names} headers={headers} activeTab={activeTab} changeTabOnClick={changeTabOnClick}
                  greeting={"Unlocked"} mode={"admin"}/>
    );
};

export default AdminPage;
