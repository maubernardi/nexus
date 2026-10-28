import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { BoardPage } from '@/pages/BoardPage/BoardPage';
import { TutorBoardPage } from '@/pages/TutorBoardPage/TutorBoardPage';

/** `/bacheca`: consultazione per il Tutor (US-901), gestione per Call Center e ADMIN (US-902). */
export const BoardRoute = () => {
  const { data: user } = useCurrentUser();
  const isTutor = (user?.roles.includes('TUTOR') ?? false) && !user?.roles.some((r) => r !== 'TUTOR');
  return isTutor ? <TutorBoardPage /> : <BoardPage />;
};
