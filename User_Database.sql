-- Create the database
CREATE DATABASE UserManagement;

-- Use the database
USE UserManagement;

-- Create a table for users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);


CREATE TABLE admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

INSERT INTO admins (username, password) VALUES ('admin', 'admin123');

-- Create Categories table
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Create Articles table
CREATE TABLE articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);


-- Insert categories
INSERT INTO categories (name) VALUES ('Technology');
INSERT INTO categories (name) VALUES ('Science');
INSERT INTO categories (name) VALUES ('Health');
INSERT INTO categories (name) VALUES ('Sports');
INSERT INTO categories (name) VALUES ('Entertainment');



-- Articles for Technology
INSERT INTO articles (title, content, category_id) VALUES 
('The Future of AI in Healthcare', 'Artificial Intelligence is transforming healthcare by improving diagnostics, patient care, and even drug development. With advanced algorithms, AI is making healthcare smarter and more efficient. Read more at: https://www.techradar.com/news/the-future-of-ai-in-healthcare', 1),
('5G Technology: What’s Next?', '5G networks are set to revolutionize mobile connectivity. From faster speeds to enabling new technologies like autonomous vehicles, 5G promises to bring the world closer together. Learn more at: https://www.cnbc.com/2023/01/25/5g-technology-whats-next.html', 1),
('Blockchain Beyond Bitcoin', 'Blockchain is no longer just for cryptocurrency. From supply chain management to secure voting systems, blockchain is becoming a game-changer in various industries. Read more at: https://www.forbes.com/sites/forbestechcouncil/2023/02/01/blockchain-beyond-bitcoin/', 1),
('How Artificial Intelligence is Shaping the Future of Finance', 'AI is streamlining financial operations and risk management. Learn about how AI is transforming the finance sector in terms of predictive analytics and algorithmic trading. Source: https://www.finextra.com/newsarticle/31075/how-ai-is-shaping-the-future-of-finance', 1),
('The Rise of Smart Homes', 'Smart homes are changing the way we live, from voice-activated devices to smart thermostats and lights. Discover how these technologies make homes more efficient and convenient. Source: https://www.digitaltrends.com/home/the-rise-of-smart-homes/', 1);

-- Articles for Science
INSERT INTO articles (title, content, category_id) VALUES 
('NASA Discovers New Exoplanets', 'NASA’s recent mission has identified new exoplanets that could be capable of sustaining life. This groundbreaking discovery opens up new avenues in the search for extraterrestrial life. Read more at: https://www.nasa.gov/press-release/nasa-discovers-new-exoplanets', 2),
('Understanding the Human Genome', 'The mapping of the human genome has brought new insights into genetics and disease prevention. Scientists are learning more about how our genes influence health. Read more at: https://www.genomeweb.com/understanding-the-human-genome', 2),
('The Theory of Relativity: A Beginner’s Guide', 'Albert Einstein’s Theory of Relativity has changed the way we understand time, space, and gravity. Explore how this theory explains the universe. Source: https://www.scientificamerican.com/article/a-beginners-guide-to-einsteins-theory-of-relativity/', 2),
('How Climate Change is Affecting Our Oceans', 'The rising global temperatures are causing sea levels to rise, and marine life is being affected. This article discusses the long-term impact of climate change on ocean ecosystems. Source: https://www.oceanconservancy.org/blog/how-climate-change-affects-oceans/', 2),
('New Breakthrough in Quantum Computing', 'Quantum computing is on the verge of revolutionizing computing power, with the potential to solve problems that are currently beyond the reach of classical computers. Source: https://www.wired.com/story/new-breakthrough-quantum-computing/', 2);

-- Articles for Health
INSERT INTO articles (title, content, category_id) VALUES 
('The Importance of Mental Health in the Workplace', 'Mental health has become a significant topic in workplace wellbeing. Companies are recognizing the importance of supporting mental health for improved productivity. Source: https://www.forbes.com/sites/forbeshumanresourcescouncil/2023/03/09/the-importance-of-mental-health-in-the-workplace/', 3),
('How to Stay Healthy During Cold and Flu Season', 'As the cold and flu season approaches, it’s important to know how to stay healthy. Simple habits like washing your hands and getting vaccinated can make a difference. Source: https://www.cdc.gov/flu/prevent/index.html', 3),
('Understanding the Benefits of a Plant-Based Diet', 'A plant-based diet can improve heart health, boost energy, and help with weight management. Learn the benefits of switching to a plant-based lifestyle. Source: https://www.healthline.com/nutrition/plant-based-diet-benefits', 3),
('5 Ways to Reduce Stress Naturally', 'Stress can have a major impact on health, but there are natural ways to reduce it. Meditation, exercise, and proper sleep can help manage stress. Source: https://www.psychologytoday.com/us/articles/5-ways-to-reduce-stress-naturally', 3),
('How Sleep Affects Your Health', 'Adequate sleep is crucial for overall health. Learn about the benefits of sleep and the long-term effects of sleep deprivation. Source: https://www.sleepfoundation.org/how-sleep-affects-your-health', 3);

-- Articles for Sports
INSERT INTO articles (title, content, category_id) VALUES 
('The Evolution of Soccer Tactics', 'Soccer tactics have evolved over the years, from the classic 4-4-2 to the more modern pressing systems. Explore how tactical innovations have changed the game. Source: https://www.theguardian.com/football/the-evolution-of-soccer-tactics', 4),
('The Future of Olympic Sports', 'As the Olympics evolve, new sports are being added while others are dropped. This article looks at the future of Olympic sports and how they’re evolving. Source: https://www.olympic.org/future-of-olympic-sports', 4),
('The Rise of Esports', 'Esports has become a global phenomenon, with millions of fans and players competing worldwide. Discover the impact of esports on traditional sports. Source: https://www.forbes.com/sites/forbestechcouncil/2023/02/15/the-rise-of-esports/', 4),
('Why Mental Training is Crucial in Sports', 'Mental toughness is just as important as physical strength. Explore the role of mental training in achieving success in sports. Source: https://www.sportspyschologytoday.com/mental-toughness-in-sports/', 4),
('How Technology is Transforming Sports', 'From performance tracking to fan engagement, technology is changing the way sports are played and experienced. Source: https://www.techradar.com/news/how-technology-is-transforming-sports', 4);

-- Articles for Entertainment
INSERT INTO articles (title, content, category_id) VALUES 
('The Impact of Streaming Services on the Entertainment Industry', 'Streaming services like Netflix and Disney+ have completely changed how we consume media. This article explores the effect streaming has had on traditional entertainment. Source: https://www.forbes.com/sites/forbestechcouncil/2023/03/09/impact-of-streaming-services-on-entertainment/', 5),
('The Evolution of Video Games', 'From the early arcade days to modern VR experiences, video games have evolved drastically. This article dives into the history and future of gaming. Source: https://www.polygon.com/the-evolution-of-video-games', 5),
('The Rise of Social Media Influencers', 'Social media influencers have become some of the most powerful figures in entertainment. Learn how this new breed of celebrity is shaping the entertainment industry. Source: https://www.socialmediaexaminer.com/how-social-media-influencers-are-changing-entertainment/', 5),
('How Reality TV Changed Entertainment', 'Reality TV has taken over the entertainment world, becoming a global sensation. Explore how this genre shaped TV and film. Source: https://www.entertainmentweekly.com/reality-tv-changed-entertainment/', 5),
('The Importance of Music in Movies', 'Music plays a crucial role in setting the tone and emotion in films. Learn about the power of soundtracks in enhancing movie experiences. Source: https://www.indiewire.com/the-importance-of-music-in-movies/', 5);






