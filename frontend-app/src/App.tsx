import "./App.css";
import * as React from "react";
import {useEffect, useState} from "react";

interface User {
    uuid: string;
}

interface Station {
    id: string;
    name: string;
    companyCodes: string[];
}

interface Route {
    startStation: string;
    endStation: string;
    departureTime: string;
}

interface SavedRoute extends Route {
    id: string;
}

let userPromise: Promise<User> | undefined;

function getOrCreateUser(): Promise<User> {
    const savedId = localStorage.getItem("user-id");
    if (savedId) {
        userPromise = Promise.resolve({uuid: savedId});
    }

    if (!userPromise) {
        userPromise = fetch("/api/users", {
            method: "POST",
            headers: {
                Accept: "application/json",
            },
        })
            .then(async (response) => {
                if (!response.ok) {
                    throw new Error(`Could not create user: ${response.status}`);
                }
                return (await response.json()) as User;
            })
            .then((user: User) => {
                localStorage.setItem("user-id", user.uuid);
                return user;
            })
            .catch((error) => {
                userPromise = undefined;
                throw error;
            });
    }

    return userPromise;
}

async function createRoute(userId: string, route: Route): Promise<SavedRoute> {
    const query = new URLSearchParams({userId: userId});
    const response = await fetch(`/api/routes?${query}`, {
        method: "POST",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify(route),
    });

    if (!response.ok) {
        throw new Error(
            `Could not create route: ${response.status} ${await response.text()}`,
        );
    }

    return (await response.json()) as SavedRoute;
}

function fetchStations(): Promise<Station[]> {
    return fetch("/api/stations").then(async (response) => {
        return (await response.json()) as Station[];
    });
}

function App() {
    const [stations, setStations] = useState<Station[]>([]);
    const [from, setFrom] = useState<Station | undefined>(undefined);
    const [to, setTo] = useState<Station | undefined>(undefined);
    const [time, setTime] = useState("08:30");
    const [alerts, setAlerts] = useState<string[]>([]);
    const [connected, setConnected] = useState(false);
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        fetchStations()
            .then(setStations)
            .catch((error) => console.error("Could not fetch stations: ", error));
    }, []);

    useEffect(() => {
        getOrCreateUser()
            .then(setUser)
            .catch((error) => console.error("Could not create user ", error));
    }, []);

    useEffect(() => {
        if (!user) {
            return;
        }

        const url = new URL(`ws://${window.location.host}/ws/${user.uuid}`);
        const ws = new WebSocket(url);
        ws.onopen = () => setConnected(true);
        ws.onclose = () => setConnected(false);
        ws.onerror = () => setConnected(false);
        ws.onmessage = (event) => {
            setAlerts((prevAlerts) => [...prevAlerts, event.data]);
        };
        return () => ws.close();
    }, [user]);

    const [savedRoutes, setSavedRoutes] = useState<SavedRoute[]>(() => {
        try {
            return JSON.parse(
                localStorage.getItem("savedRoutes") ?? "[]",
            ) as SavedRoute[];
        } catch {
            return [];
        }
    });
    const saveRoute = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!from || !to || from == to || !user) {
            return;
        }

        const route: Route = {
            startStation: from?.id,
            endStation: to?.id,
            departureTime: time,
        };

        const created: SavedRoute = await createRoute(user.uuid, route);
        const next: SavedRoute[] = [created, ...savedRoutes];
        setSavedRoutes(next);

        localStorage.setItem("savedRoutes", JSON.stringify(next));
        setFrom(undefined);
        setTo(undefined);
    };

    const deleteRoute = (id: string) => {
        var filtered = savedRoutes.filter((sr) => sr.id !== id);
        setSavedRoutes(filtered);
        localStorage.setItem("savedRoutes", JSON.stringify(filtered));
    };

    return (
        <>
            <header className="topbar">
                <h1> Berlin Transit Tracker </h1>
                <div className="connection" aria-live="polite">
                    <span className={connected ? "dot connected" : "dot"}/>
                    WebSocket {connected ? "connected" : "disconnected"}
                </div>
            </header>

            <main className="layout">
                <section className="panel">
                    <h2>Add a commute</h2>

                    <form onSubmit={saveRoute}>
                        <label htmlFor="from">Station A</label>
                        <select
                            id="from"
                            value={from?.id ?? ""}
                            onChange={(event) =>
                                setFrom(
                                    stations.find((station) => station.id === event.target.value),
                                )
                            }
                            required
                        >
                            <option value="" disabled>
                                Select a station
                            </option>
                            {stations.map((station) => (
                                <option key={station.id} value={station.id}>
                                    {station.name}
                                </option>
                            ))}
                        </select>

                        <label htmlFor="to">Station B</label>
                        <select
                            id="to"
                            value={to?.id ?? ""}
                            onChange={(event) =>
                                setTo(
                                    stations.find((station) => station.id === event.target.value),
                                )
                            }
                            required
                        >
                            <option value="" disabled>
                                Select a station
                            </option>
                            {stations.map((station) => (
                                <option key={station.id} value={station.id}>
                                    {station.name}
                                </option>
                            ))}
                        </select>

                        <label htmlFor="time">Departure time</label>
                        <input
                            id="time"
                            type="time"
                            value={time}
                            onChange={(event) => setTime(event.target.value)}
                            required
                        />

                        <button type="submit" disabled={!from || !to || from == to}>
                            Save Route
                        </button>
                    </form>
                </section>

                <section className="commutes">
                    <h2>Your commutes</h2>
                    {savedRoutes.length === 0 ? (
                        <p className="empty">No saved commutes yet.</p>
                    ) : (
                        savedRoutes.map((route) => (
                            <article className="commute-card" key={route.id}>
                                <div>
                                    <strong>
                                        {
                                            stations.find(
                                                (station) => station.id === route.startStation,
                                            )?.name
                                        }
                                        {" → "}
                                        {
                                            stations.find(
                                                (station) => station.id === route.endStation,
                                            )?.name
                                        }
                                    </strong>
                                    <p>{route.departureTime}</p>
                                </div>
                                <button
                                    className="delete-button"
                                    onClick={() => deleteRoute(route.id)}
                                >
                                    Delete
                                </button>
                            </article>
                        ))
                    )}
                </section>

                <section className="alerts" aria-live="polite">
                    <h2>Live alerts</h2>
                    {alerts.length == 0 ? (
                        <p className="empty">No alerts yet</p>
                    ) : (
                        <ul>
                            {alerts.map((alert, index) => (
                                <li key={index}>{alert}</li>
                            ))}
                        </ul>
                    )}
                </section>
            </main>
        </>
    );
}

export default App;
