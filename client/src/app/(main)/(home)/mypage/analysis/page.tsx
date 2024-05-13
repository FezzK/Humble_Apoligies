import type { Metadata } from 'next';
import MyGraph from '../_components/MyGraph';
import MyAnalysis from '../_components/MyAnalysis';
import MyStrength from '../_components/MyStrength';
import { useGetAbilities } from '@/queries/get-abilities';

export const metadata: Metadata = {
  title: '분석 보고서',
  description: '문해력 학습 서비스 심심한 사과의 분석 보고서',
};

export default async function Analysis() {
  const { data: abilities } = useGetAbilities();

  const ability: number[] = abilities?.data[0] ?? [];

  return (
    <div className='w-full'>
      <div className='flex flex-col '>
        <div className='pt-5 pb-2 text-lg text-ourBlack'>종합 평가</div>
        <div className='flex gap-4'>
          <div className='flex flex-col items-center flex-1 px-5 pb-5 bg-white rounded-2xl'>
            <div className='w-full py-3 font-bold text-ourDarkGray'>독해 핵심 능력</div>
            <div className='flex items-center justify-center w-52 h-52'>
              <MyGraph ability={ability} />
            </div>
            <MyAnalysis ability={ability} />
          </div>
          <div className='w-1/3 px-5 pb-5 bg-white rounded-2xl'>
            <div className='w-full py-3 font-bold text-ourDarkGray'>강점 & 약점</div>
            <MyStrength abilities={abilities?.data || [[]]} />
          </div>
        </div>
      </div>
    </div>
  );
}
